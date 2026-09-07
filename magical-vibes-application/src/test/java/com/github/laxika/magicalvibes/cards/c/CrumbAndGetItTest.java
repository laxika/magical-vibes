package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({CrumbAndGetIt.class, GrizzlyBears.class})
class CrumbAndGetItTest extends BaseCardTest {

    @Test
    @DisplayName("Without the gift, the creature gets +2/+2 only")
    void withoutGiftOnlyBoostsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(bear.getId(), false);

        Permanent resolvedBear = findPermanent(player1, "Grizzly Bears");
        assertThat(resolvedBear.getPowerModifier()).isEqualTo(2);
        assertThat(resolvedBear.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, resolvedBear, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Promising the gift gives an opponent Food and grants indestructible")
    void promisingGiftCreatesFoodAndGrantsIndestructible() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(bear.getId(), true);

        Permanent resolvedBear = findPermanent(player1, "Grizzly Bears");
        assertThat(resolvedBear.getPowerModifier()).isEqualTo(2);
        assertThat(resolvedBear.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, resolvedBear, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("The spell can target only a creature controlled by its caster")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, targetId, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void cast(UUID targetId, boolean giftPromised) {
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstantWithGift(player1, 0, targetId, giftPromised);
        harness.passBothPriorities();
    }
}
