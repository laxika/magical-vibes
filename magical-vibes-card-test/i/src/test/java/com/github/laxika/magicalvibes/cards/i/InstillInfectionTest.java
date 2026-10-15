package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.m.Memnite;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({InstillInfection.class, CarapaceForger.class, AccordersShield.class, Memnite.class})
class InstillInfectionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on target creature and draws a card")
    void putsCounterAndDrawsCard() {
        harness.addToBattlefield(player2, new CarapaceForger());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new InstillInfection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Carapace Forger");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = findPermanent(player2, "Carapace Forger");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Can kill a 1/1 creature with the -1/-1 counter")
    void killsOneOneCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1); // 2/2 with -1/-1 = effectively 1/1
        UUID bearsId = bears.getId();

        harness.setHand(player1, List.of(new InstillInfection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new CarapaceForger());
        harness.setHand(player1, List.of(new InstillInfection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Carapace Forger");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Instill Infection");
    }

    @Test
    @DisplayName("Fizzles and does not draw when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new CarapaceForger());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new InstillInfection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Carapace Forger");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player1, "Instill Infection");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new AccordersShield());
        harness.setHand(player1, List.of(new InstillInfection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID spellbookId = harness.getPermanentId(player2, "Accorder's Shield");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draws a card even when the counter kills the target")
    void drawsWhenCounterKillsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.setHand(player1, List.of(new InstillInfection()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertInGraveyard(player2, "Memnite");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new InstillInfection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Carapace Forger");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
