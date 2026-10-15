package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.k.KasminasTransmutation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldspanDragon.class, GiantGrowth.class, FrostBite.class, KasminasTransmutation.class})
class GoldspanDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Treasure token")
    void attackCreatesTreasureToken() {
        addCreatureReady(player1, new GoldspanDragon());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Becoming the target of a spell creates a Treasure token")
    void beingTargetedBySpellCreatesTreasureToken() {
        Permanent dragon = addCreatureReady(player1, new GoldspanDragon());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, dragon.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Treasures gain an additional ability that produces two mana")
    void treasureGainsTwoManaAbility() {
        addCreatureReady(player1, new GoldspanDragon());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's spell creates Treasure for the Dragon's controller")
    void opponentSpellCreatesTreasureForController() {
        Permanent dragon = addCreatureReady(player1, new GoldspanDragon());
        harness.setHand(player2, List.of(new FrostBite()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, dragon.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Goldspan Dragon");
    }

    @Test
    @DisplayName("Treasure retains its original one-mana ability")
    void treasureRetainsOriginalManaAbility() {
        addCreatureReady(player1, new GoldspanDragon());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Treasures lose the granted mana ability when the Dragon leaves")
    void treasuresRevertWhenDragonDies() {
        Permanent dragon = addCreatureReady(player1, new GoldspanDragon());
        harness.setHand(player2, List.of(new FrostBite(), new FrostBite()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, dragon.getId());
        resolveAllTriggers();
        harness.castInstant(player2, 0, dragon.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Goldspan Dragon");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("A Dragon does not grant its mana ability to opposing Treasures")
    void opposingTreasuresDoNotGainManaAbility() {
        addCreatureReady(player1, new GoldspanDragon());
        Permanent opposingDragon = addCreatureReady(player2, new GoldspanDragon());
        harness.setHand(player1, List.of(new FrostBite(), new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, opposingDragon.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, opposingDragon.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Goldspan Dragon");
        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Treasure cannot pay the granted tap cost")
    void tappedTreasureCannotActivateGrantedAbility() {
        addCreatureReady(player1, new GoldspanDragon());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        findPermanent(player1, "Treasure").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("A Dragon that has lost all abilities does not trigger when targeted")
    void abilityRemovalSuppressesSpellTargetTrigger() {
        Permanent dragon = addCreatureReady(player1, new GoldspanDragon());
        harness.setHand(player1, List.of(new KasminasTransmutation(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, dragon.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.castInstant(player1, 0, dragon.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertOnBattlefield(player1, "Goldspan Dragon");
    }
}
