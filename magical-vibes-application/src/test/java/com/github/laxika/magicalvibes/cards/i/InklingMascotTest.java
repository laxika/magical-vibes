package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({InklingMascot.class, GrizzlyBears.class, HillGiant.class, Shock.class, Assassinate.class})
class InklingMascotTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant that targets a creature gives the Mascot flying and surveils")
    void reparteeGrantsFlyingAndSurveils() {
        Permanent mascot = harness.addToBattlefieldAndReturn(player1, new InklingMascot());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities(); // resolve Repartee trigger — grants flying, queues surveil may
        harness.handleMayAbilityChosen(player1, true); // surveil: put top card into graveyard

        assertThat(mascot.getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent mascot = harness.addToBattlefieldAndReturn(player1, new InklingMascot());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline surveil

        assertThat(mascot.getGrantedKeywords()).contains(Keyword.FLYING);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities(); // Resolve Shock before advancing the turn.

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(mascot.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new InklingMascot());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    void sorceryTargetingOwnCreatureTriggersRepartee() {
        Permanent mascot = harness.addToBattlefieldAndReturn(player1, new InklingMascot());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(mascot.getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void opponentsInstantDoesNotTriggerRepartee() {
        harness.addToBattlefield(player1, new InklingMascot());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, giant.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    void creatureSpellDoesNotTriggerRepartee() {
        harness.addToBattlefield(player1, new InklingMascot());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    void emptyLibraryDoesNotPreventFlying() {
        Permanent mascot = harness.addToBattlefieldAndReturn(player1, new InklingMascot());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(mascot.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void surveilsEvenIfMascotDiesBeforeTriggerResolves() {
        Permanent mascot = harness.addToBattlefieldAndReturn(player1, new InklingMascot());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());
        harness.castInstant(player2, 0, mascot.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Inkling Mascot");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
