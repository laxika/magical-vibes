package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenjuOfTheCedarsTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted Forest can become a 4/4 green Spirit while remaining a land")
    void animatesEnchantedForest() {
        Permanent forest = addEnchantedForest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
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
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("When the enchanted Forest dies, Genju may return from the graveyard to its owner's hand")
    void returnsToHandWhenEnchantedForestDies() {
        Permanent forest = addEnchantedForest(player1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));
        resolveStackFully();
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
        resolveStackFully();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genju of the Cedars");
        harness.assertNotInHand(player1, "Genju of the Cedars");
    }

    @Test
    @DisplayName("Genju can enchant only a Forest")
    void cannotEnchantNonForest() {
        harness.addToBattlefield(player1, new Island());
        Permanent island = findPermanent(player1, "Island");
        harness.setHand(player1, List.of(new GenjuOfTheCedars()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addEnchantedForest(Player controller) {
        harness.addToBattlefield(controller, new Forest());
        Permanent forest = findPermanent(controller, "Forest");
        Permanent aura = new Permanent(new GenjuOfTheCedars());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return forest;
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
