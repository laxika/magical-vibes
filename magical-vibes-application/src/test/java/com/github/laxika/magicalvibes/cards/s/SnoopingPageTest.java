package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnoopingPage.class, HillGiant.class, Shock.class, Forest.class, SpittingEarth.class})
class SnoopingPageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant that targets a creature makes the Page unblockable this turn")
    void reparteeMakesUnblockable() {
        Permanent page = addCreatureReady(player1, new SnoopingPage());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities(); // resolve Repartee trigger

        assertThat(page.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable resets at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        Permanent page = addCreatureReady(player1, new SnoopingPage());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        assertThat(page.isCantBeBlocked()).isTrue();

        harness.passBothPriorities(); // resolve Shock before advancing the turn
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(page.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Dealing combat damage to a player draws a card and loses 1 life")
    void combatDamageDrawsAndLosesLife() {
        Permanent page = addCreatureReady(player1, new SnoopingPage());
        page.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Page is 2/3 — player2 takes 2 combat damage
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        // Both instructions belong to the same ability and resolve together.
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        addCreatureReady(player1, new SnoopingPage());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    @DisplayName("A sorcery targeting the Page itself triggers Repartee")
    void sorceryTargetingSelfTriggersRepartee() {
        Permanent page = addCreatureReady(player1, new SnoopingPage());
        harness.setHand(player1, List.of(new SpittingEarth()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, page.getId());
        harness.passBothPriorities();

        assertThat(page.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("An opponent's instant targeting a creature does not trigger Repartee")
    void opponentSpellDoesNotTriggerRepartee() {
        Permanent page = addCreatureReady(player1, new SnoopingPage());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, page.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(page.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Casting a creature does not trigger Repartee")
    void creatureSpellDoesNotTriggerRepartee() {
        addCreatureReady(player1, new SnoopingPage());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    @DisplayName("Combat damage creates one ability containing both draw and life loss")
    void combatDamageCreatesOneTrigger() {
        Permanent page = addCreatureReady(player1, new SnoopingPage());
        page.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.resolveCombatDamage();

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);
    }
}
