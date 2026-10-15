package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrumbAndGetIt.class, BarkformHarvester.class})
class CrumbAndGetItTest extends BaseCardTest {

    @Test
    @DisplayName("Without the gift, the creature gets +2/+2 only")
    void withoutGiftOnlyBoostsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        cast(bear.getId(), false);

        Permanent resolvedBear = findPermanent(player1, "Barkform Harvester");
        assertThat(resolvedBear.getPowerModifier()).isEqualTo(2);
        assertThat(resolvedBear.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, resolvedBear, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Promising the gift gives an opponent Food and grants indestructible")
    void promisingGiftCreatesFoodAndGrantsIndestructible() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        cast(bear.getId(), true);

        Permanent resolvedBear = findPermanent(player1, "Barkform Harvester");
        assertThat(resolvedBear.getPowerModifier()).isEqualTo(2);
        assertThat(resolvedBear.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, resolvedBear, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("The spell can target only a creature controlled by its caster")
    void cannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, creature.getId(), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("The boost and indestructible expire at the end of the turn")
    void giftBonusesExpireAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        cast(creature.getId(), true);

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("The opponent can sacrifice the gifted Food for three life")
    void giftedFoodCanBeUsedImmediately() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.setLife(player2, 10);
        cast(creature.getId(), true);

        assertThat(countPermanents(player2, "Food")).isEqualTo(1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);

        harness.assertNotOnBattlefield(player2, "Food");
        harness.assertLife(player2, 10);
        harness.passBothPriorities();
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("No Food is given when the spell's only target has left the battlefield")
    void invalidTargetPreventsGift() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstantWithGift(player1, 0, creature.getId(), true);

        harness.assertNotOnBattlefield(player2, "Food");
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Food");
        harness.assertInGraveyard(player1, "Crumb and Get It");
    }

    private void cast(UUID targetId, boolean giftPromised) {
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstantWithGift(player1, 0, targetId, giftPromised);
        harness.passBothPriorities();
    }
}
