package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KomaWorldEater.class, Cancel.class, GiantGrowth.class, LlanowarElves.class})
class KomaWorldEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        KomaWorldEater koma = new KomaWorldEater();
        harness.setHand(player1, List.of(koma));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, koma.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Koma, World-Eater");
        harness.assertNotInGraveyard(player1, "Koma, World-Eater");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Creates four 3/3 blue Serpent tokens when dealing combat damage to a player")
    void createsKomasCoilsOnCombatDamage() {
        addCreatureReady(player1, new KomaWorldEater());
        declareAttackers(List.of(0));

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Koma's Coil")).hasSize(4)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(3);
                    assertThat(token.getCard().getToughness()).isEqualTo(3);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
                });
    }

    @Test
    @DisplayName("Does not create tokens without combat damage to a player")
    void doesNotCreateKomasCoilsWithoutCombatDamageToPlayer() {
        addCreatureReady(player1, new KomaWorldEater());
        Permanent blocker = addCreatureReady(player2, new KomaWorldEater());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 8));
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Koma's Coil")).isEmpty();
    }

    @Test
    void trampleDamageCreatesFourCoilsRegardlessOfDamageAmount() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KomaWorldEater());
        Permanent blocker = addCreatureReady(player2, new LlanowarElves());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 7));
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(findPermanents(player1, "Koma's Coil")).hasSize(4);
    }

    @Test
    void wardCountersSpellWhenOpponentHasOnlyThreeManaLeft() {
        Permanent koma = prepareOpponentGrowth(4);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, koma)).isEqualTo(8);
        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void payingFourManaForWardAllowsSpellToResolve() {
        Permanent koma = prepareOpponentGrowth(5);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, koma)).isEqualTo(11);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void decliningWardPaymentCountersSpell() {
        Permanent koma = prepareOpponentGrowth(5);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, koma)).isEqualTo(8);
        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    void wardDoesNotTaxControllersOwnSpell() {
        Permanent koma = addCreatureReady(player1, new KomaWorldEater());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, koma.getId());

        assertThat(gqs.getEffectivePower(gd, koma)).isEqualTo(11);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent prepareOpponentGrowth(int availableMana) {
        Permanent koma = addCreatureReady(player1, new KomaWorldEater());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, availableMana);
        harness.castInstant(player2, 0, koma.getId());
        return koma;
    }
}
