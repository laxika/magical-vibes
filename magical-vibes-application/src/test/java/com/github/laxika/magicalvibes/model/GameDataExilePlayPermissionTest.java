package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GameDataExilePlayPermissionTest {

    private GameData gd;
    private UUID playerId;
    private Card exiled;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", playerId, "Player1");
        exiled = new Card();
        exiled.setName("Exiled");
        gd.addToExile(playerId, exiled);
    }

    @Test
    @DisplayName("A granted permission names its holder and ends as the card leaves exile (CR 400.7)")
    void permissionEndsAsTheCardLeavesExile() {
        gd.grantExilePlayPermission(exiled.getId(), playerId, Duration.PERMANENT);

        assertThat(gd.exilePlayPermissionHolder(exiled.getId())).isEqualTo(playerId);

        gd.removeFromExile(exiled.getId());

        assertThat(gd.exilePlayPermissionHolder(exiled.getId())).isNull();
    }

    @Test
    @DisplayName("Granting again replaces the card's permission, and riders are part of the permission")
    void grantingAgainReplacesThePermission() {
        gd.grantExilePlayPermission(exiled.getId(), playerId, Duration.PERMANENT);
        gd.setExilePlayWithoutPayingManaCost(exiled.getId(), true);
        assertThat(gd.exilePlayWithoutPayingManaCost(exiled.getId())).isTrue();

        gd.grantExilePlayPermission(exiled.getId(), playerId, Duration.UNTIL_END_OF_TURN);

        assertThat(gd.exilePlayWithoutPayingManaCost(exiled.getId())).isFalse();
        assertThat(gd.exilePlayPermissionDuration(exiled.getId())).isEqualTo(Duration.UNTIL_END_OF_TURN);
        assertThat(gd.floatingEffectsOfTypes(ExilePlayPermissionEffect.class)).hasSize(1);
    }

    @Test
    @DisplayName("A rider of a card with no permission is an effect of its own that ends with the card")
    void riderWithoutPermissionIsItsOwnEffect() {
        gd.setExilePlayWithoutPayingManaCost(exiled.getId(), true);
        gd.setExilePlayCostModifier(exiled.getId(), new ExilePlayCostModifier(playerId, null, 0, "{1}"));

        assertThat(gd.exilePlayPermissionHolder(exiled.getId())).isNull();
        assertThat(gd.exilePlayWithoutPayingManaCost(exiled.getId())).isTrue();
        assertThat(gd.exilePlayCostModifier(exiled.getId()).replacementManaCost()).isEqualTo("{1}");

        gd.removeFromExile(exiled.getId());

        assertThat(gd.exilePlayWithoutPayingManaCost(exiled.getId())).isFalse();
        assertThat(gd.exilePlayCostModifier(exiled.getId())).isNull();
    }

    @Test
    @DisplayName("A permission that begins later is invisible until it begins (CR 702.185a)")
    void scheduledPermissionIsInvisibleUntilItBegins() {
        gd.grantExilePlayPermission(exiled.getId(), playerId, Duration.AFTER_THIS_TURN);

        assertThat(gd.exilePlayPermissionHolder(exiled.getId())).isNull();
        assertThat(gd.exilePlayPermissionDuration(exiled.getId())).isEqualTo(Duration.AFTER_THIS_TURN);
        assertThat(gd.updateExilePlayPermission(exiled.getId(), granted -> granted.withAnyManaType(true))).isTrue();

        gd.startScheduledFloatingEffects(effect -> true);

        assertThat(gd.exilePlayPermission(exiled.getId()).orElseThrow().anyManaType()).isTrue();
    }

    @Test
    @DisplayName("The shared quota of a group of permissions goes with its last permission")
    void groupQuotaGoesWithItsLastPermission() {
        Card other = new Card();
        other.setName("Other");
        gd.addToExile(playerId, other);
        gd.grantExilePlayPermission(exiled.getId(), playerId, Duration.UNTIL_END_OF_TURN);
        gd.grantExilePlayPermission(other.getId(), playerId, Duration.UNTIL_END_OF_TURN);
        UUID groupId = UUID.randomUUID();
        gd.registerExilePlayPermissionGroup(groupId, 1, List.of(exiled.getId(), other.getId()));

        assertThat(gd.consumeExilePlayPermission(exiled.getId())).isTrue();
        assertThat(gd.hasExilePlayPermissionRemaining(other.getId())).isFalse();

        gd.endExilePlayPermission(exiled.getId());
        assertThat(gd.exilePlayPermissionGroupUsesRemaining).containsKey(groupId);

        gd.endExilePlayPermission(other.getId());
        assertThat(gd.exilePlayPermissionGroupUsesRemaining).doesNotContainKey(groupId);
    }

    @Test
    @DisplayName("Look permissions are one per viewer and end as the card leaves exile")
    void lookPermissionsEndWithTheCard() {
        UUID other = UUID.randomUUID();
        gd.grantExileLookPermission(exiled.getId(), playerId);
        gd.grantExileLookPermission(exiled.getId(), playerId);
        gd.grantExileLookPermission(exiled.getId(), other);

        assertThat(gd.exileLookViewers(exiled.getId())).containsExactlyInAnyOrder(playerId, other);

        gd.removeFromExile(exiled.getId());

        assertThat(gd.exileLookViewers(exiled.getId())).isEmpty();
    }
}
