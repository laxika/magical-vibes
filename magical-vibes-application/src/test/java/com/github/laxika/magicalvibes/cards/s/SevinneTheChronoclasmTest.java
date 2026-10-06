package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.Lunge;
import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SevinneTheChronoclasm.class, Lunge.class, ThinkTwice.class, DeepAnalysis.class})
class SevinneTheChronoclasmTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage dealt to Sevinne")
    void preventsDamageToSelf() {
        Permanent sevinne = addCreatureReady(player1, new SevinneTheChronoclasm());
        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(sevinne.getId(), player2.getId()));

        assertThat(sevinne.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Sevinne, the Chronoclasm");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Copies the first instant or sorcery cast from the graveyard each turn")
    void copiesFirstGraveyardSpellEachTurn() {
        addCreatureReady(player1, new SevinneTheChronoclasm());
        ThinkTwice first = new ThinkTwice();
        ThinkTwice second = new ThinkTwice();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castFromGraveyard(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Does not copy a second graveyard spell in the same turn")
    void doesNotCopySecondGraveyardSpellEachTurn() {
        addCreatureReady(player1, new SevinneTheChronoclasm());
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castFromGraveyard(player1, 0);
        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }

    @Test
    void doesNotCopyWhenFirstGraveyardSpellWasCastBeforeEntering() {
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.setLibrary(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        addCreatureReady(player1, new SevinneTheChronoclasm());
        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    void handCastDoesNotUseUpFirstGraveyardSpell() {
        addCreatureReady(player1, new SevinneTheChronoclasm());
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        resolveAllTriggers();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    void doesNotCopyOpponentsGraveyardSpell() {
        addCreatureReady(player1, new SevinneTheChronoclasm());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromGraveyard(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    void copiesSorceryAndAllowsNewTargetWithoutPayingFlashbackLifeAgain() {
        addCreatureReady(player1, new SevinneTheChronoclasm());
        harness.setGraveyard(player1, List.of(new DeepAnalysis()));
        harness.setLibrary(player1, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.setLibrary(player2, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFlashback(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 17);
    }

    @Test
    void preventsCombatDamageToSelf() {
        addCreatureReady(player1, new SevinneTheChronoclasm());
        Permanent blocker = addCreatureReady(player2, new SevinneTheChronoclasm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Sevinne, the Chronoclasm");
        harness.assertOnBattlefield(player2, "Sevinne, the Chronoclasm");
        harness.assertLife(player2, 20);
    }

    @Test
    void copiesAgainOnOpponentsTurn() {
        addCreatureReady(player1, new SevinneTheChronoclasm());
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.setLibrary(player1, List.of(new ThinkTwice(), new ThinkTwice(), new ThinkTwice()));
        harness.setLibrary(player2, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }
}
