package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OperaLoveSong.class, GrizzlyBears.class, Mountain.class})
class OperaLoveSongTest extends BaseCardTest {

    @Test
    @DisplayName("Exile mode exiles the top two cards and grants play permission until your next end step")
    void exileMode() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        cast(0, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(first.getId(), second.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKeys(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Boost mode gives one or two target creatures +2/+0 until end of turn")
    void boostMode() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1, List.of(first.getId(), second.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost mode allows exactly one target creature")
    void singleTargetAllowed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost mode requires at least one creature target")
    void requiresAtLeastOneTarget() {
        harness.setHand(player1, List.of(new OperaLoveSong()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost mode rejects a noncreature target")
    void requiresCreatureTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new OperaLoveSong()));
        addMana();

        UUID targetId = target.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new OperaLoveSong()));
        addMana();
        harness.castModalInstant(player1, 0, mode, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
