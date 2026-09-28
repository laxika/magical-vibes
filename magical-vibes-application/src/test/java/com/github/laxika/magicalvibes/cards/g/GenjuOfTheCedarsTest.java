package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenjuOfTheCedars.class, Forest.class, Swamp.class})
class GenjuOfTheCedarsTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted Forest can become a 4/4 green Spirit while remaining a land")
    void animatesEnchantedForest() {
        Permanent forest = addEnchantedForest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        activateGenju(player1);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
        assertThat(forest.getTransientSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("The animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        Permanent forest = addEnchantedForest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        activateGenju(player1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("When the enchanted Forest dies, Genju may return from your graveyard to your hand")
    void returnsToHandWhenEnchantedForestDies() {
        Permanent forest = addEnchantedForest(player1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Genju of the Cedars");
        harness.assertNotInGraveyard(player1, "Genju of the Cedars");
        harness.assertNotOnBattlefield(player1, "Genju of the Cedars");
    }

    @Test
    @DisplayName("Genju stays in the graveyard when its Forest death trigger is declined")
    void decliningReturnLeavesGenjuInGraveyard() {
        Permanent forest = addEnchantedForest(player1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genju of the Cedars");
        harness.assertNotInHand(player1, "Genju of the Cedars");
    }

    @Test
    @DisplayName("Genju can enchant only a Forest")
    void cannotEnchantNonForest() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = findPermanent(player1, "Swamp");
        harness.setHand(player1, List.of(new GenjuOfTheCedars()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Genju controller can activate it on an opponent's Forest")
    void controllerCanAnimateOpponentsForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GenjuOfTheCedars()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activateGenju(player1);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
    }

    private Permanent addEnchantedForest(Player controller) {
        Permanent forest = harness.addToBattlefieldAndReturn(controller, new Forest());
        GenjuOfTheCedars genju = new GenjuOfTheCedars();
        genju.setOwnerId(controller.getId());
        harness.setHand(controller, List.of(genju));
        harness.addMana(controller, ManaColor.GREEN, 1);
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(controller, 0, forest.getId());
        harness.passBothPriorities();
        return forest;
    }

    private void activateGenju(Player controller) {
        int genjuIndex = gd.playerBattlefields.get(controller.getId()).indexOf(
                findPermanent(controller, "Genju of the Cedars"));
        harness.activateAbility(controller, genjuIndex, 0, null, null);
    }

}
