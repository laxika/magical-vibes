package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VolcanicHammer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({ForumNecroscribe.class, GrizzlyBears.class, HillGiant.class, Shock.class,
        VolcanicHammer.class, ProdigalSorcerer.class})
class ForumNecroscribeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant that targets a creature returns a creature card from graveyard to battlefield")
    void reparteeReturnsCreature() {
        harness.addToBattlefield(player1, new ForumNecroscribe());
        harness.addToBattlefield(player1, new HillGiant());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.passBothPriorities(); // resolve return trigger
        harness.passBothPriorities(); // resolve Shock

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new ForumNecroscribe());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Ward triggers when an opponent targets Forum Necroscribe")
    void wardTriggersOnOpponentSpell() {
        Permanent necroscribe = addReadyNecroscribe();

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, necroscribe.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Forum Necroscribe");
    }

    @Test
    @DisplayName("Ward counters the opponent's spell when they have no card to discard")
    void wardCountersWhenNoCards() {
        Permanent necroscribe = addReadyNecroscribe();

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock())); // Shock is the only card
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, necroscribe.getId());
        harness.passBothPriorities(); // resolve Ward trigger — no cards to discard

        harness.assertInGraveyard(player2, "Shock");
        assertThat(necroscribe.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ward counters the opponent's spell when they decline to discard")
    void wardCountersWhenDeclined() {
        Permanent necroscribe = addReadyNecroscribe();

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, necroscribe.getId());
        harness.passBothPriorities(); // resolve Ward trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false); // decline to discard

        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(necroscribe.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Opponent may discard a card to prevent the counter")
    void wardPreventedByDiscard() {
        Permanent necroscribe = addReadyNecroscribe();

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, necroscribe.getId());
        harness.passBothPriorities(); // resolve Ward trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true); // choose to discard

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0); // discard Grizzly Bears

        // Discard happened (not a counter) — Shock is not countered
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A sorcery targeting an opposing creature reanimates before the sorcery resolves")
    void sorceryTargetingOpponentCreatureTriggersRepartee() {
        harness.addToBattlefield(player1, new ForumNecroscribe());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new VolcanicHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, giant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A Repartee target removed from the graveyard is not reanimated")
    void removedGraveyardTargetIsNotReturned() {
        harness.addToBattlefield(player1, new ForumNecroscribe());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Repartee cannot return a noncreature or a creature from an opponent's graveyard")
    void noLegalCreatureInControllersGraveyard() {
        harness.addToBattlefield(player1, new ForumNecroscribe());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature-targeting instant does not trigger Repartee")
    void opponentsSpellDoesNotTriggerRepartee() {
        harness.addToBattlefield(player1, new ForumNecroscribe());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, giant.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Targeting your own Necroscribe triggers Repartee without triggering ward")
    void ownSpellDoesNotTriggerWard() {
        Permanent necroscribe = harness.addToBattlefieldAndReturn(player1, new ForumNecroscribe());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, necroscribe.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(necroscribe.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability when they cannot discard")
    void wardCountersActivatedAbility() {
        Permanent necroscribe = harness.addToBattlefieldAndReturn(player1, new ForumNecroscribe());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        beginOpponentTurn();
        harness.setHand(player2, List.of());

        harness.activateAbility(player2, 0, null, necroscribe.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(necroscribe.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Prodigal Sorcerer");
    }

    @Test
    @DisplayName("Paying ward's discard cost allows the targeted spell to resolve")
    void payingWardAllowsSpellToResolve() {
        Permanent necroscribe = harness.addToBattlefieldAndReturn(player1, new ForumNecroscribe());
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, necroscribe.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(necroscribe.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReadyNecroscribe() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ForumNecroscribe());
        perm.setSummoningSick(false);
        return perm;
    }

    private void beginOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
