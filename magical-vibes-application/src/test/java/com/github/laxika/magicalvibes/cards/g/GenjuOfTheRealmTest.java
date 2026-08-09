package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
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

class GenjuOfTheRealmTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted land becomes a legendary 8/12 Spirit with trample while remaining a land")
    void animatesEnchantedLand() {
        Permanent island = addEnchantedIsland(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, island)).isTrue();
        assertThat(gqs.isLand(gd, island)).isTrue();
        assertThat(gqs.getEffectivePower(gd, island)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, island)).isEqualTo(12);
        assertThat(gqs.effectiveCreatureSubtypes(gd, island)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, island, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, island, CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    @DisplayName("The animated land stops being a creature and legendary at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent island = addEnchantedIsland(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, island)).isFalse();
        assertThat(gqs.hasEffectiveSupertype(gd, island, CardSupertype.LEGENDARY)).isFalse();
        assertThat(gqs.hasKeyword(gd, island, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("When the enchanted land dies, Genju may return from the graveyard to its owner's hand")
    void returnsToHandWhenEnchantedLandDies() {
        addEnchantedIsland(player1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Island")));
        resolveStackFully();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Genju of the Realm");
        harness.assertNotInGraveyard(player1, "Genju of the Realm");
        harness.assertNotOnBattlefield(player1, "Genju of the Realm");
    }

    @Test
    @DisplayName("Genju stays in the graveyard when its land death trigger is declined")
    void decliningReturnLeavesGenjuInGraveyard() {
        addEnchantedIsland(player1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Island")));
        resolveStackFully();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genju of the Realm");
        harness.assertNotInHand(player1, "Genju of the Realm");
    }

    @Test
    @DisplayName("Genju can enchant only a land")
    void cannotEnchantNonland() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new GenjuOfTheRealm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addEnchantedIsland(Player controller) {
        harness.addToBattlefield(controller, new Island());
        Permanent island = findPermanent(controller, "Island");
        Permanent aura = new Permanent(new GenjuOfTheRealm());
        aura.setAttachedTo(island.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return island;
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
