package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaskedAdmirers.class, GrizzlyBears.class, Spellbook.class,
        MakeshiftMannequin.class, NamelessInversion.class})
class MaskedAdmirersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void entersDrawsCard() {
        MaskedAdmirers admirers = new MaskedAdmirers();
        harness.setHand(player1, List.of(admirers));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting a creature spell triggers may-pay prompt from graveyard")
    void creatureCastTriggersMayPayPrompt() {
        MaskedAdmirers admirers = new MaskedAdmirers();
        harness.setGraveyard(player1, List.of(admirers));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting and paying {G}{G} returns Masked Admirers from graveyard to hand")
    void acceptAndPayReturnsToHand() {
        MaskedAdmirers admirers = new MaskedAdmirers();
        harness.setGraveyard(player1, List.of(admirers));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(admirers.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(admirers.getId()));
    }

    @Test
    @DisplayName("Declining may-pay keeps Masked Admirers in graveyard")
    void declineKeepsInGraveyard() {
        MaskedAdmirers admirers = new MaskedAdmirers();
        harness.setGraveyard(player1, List.of(admirers));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(admirers.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(admirers.getId()));
    }

    @Test
    @DisplayName("Casting a non-creature spell does not trigger the graveyard ability")
    void nonCreatureCastDoesNotTrigger() {
        MaskedAdmirers admirers = new MaskedAdmirers();
        harness.setGraveyard(player1, List.of(admirers));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(admirers.getId()));
    }

    @Test
    @DisplayName("Does not trigger while Masked Admirers is on the battlefield")
    void doesNotTriggerFromBattlefield() {
        harness.addToBattlefield(player1, new MaskedAdmirers());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        // Only the creature spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's creature cast does not trigger the graveyard ability")
    void opponentCreatureCastDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new MaskedAdmirers()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Masked Admirers");
    }

    @Test
    @DisplayName("Each copy requires its own payment and returns only itself")
    void copiesReturnIndependently() {
        MaskedAdmirers first = new MaskedAdmirers();
        MaskedAdmirers second = new MaskedAdmirers();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getId())
                .isIn(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getId())
                .isNotEqualTo(gd.playerHands.get(player1.getId()).getFirst().getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An old trigger cannot return Admirers after it leaves and reenters the graveyard")
    void oldTriggerCannotReturnNewGraveyardObject() {
        MaskedAdmirers admirers = new MaskedAdmirers();
        harness.setGraveyard(player1, List.of(admirers));
        harness.setHand(player1, List.of(new GrizzlyBears(), new MakeshiftMannequin(),
                new NamelessInversion()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, admirers.getId());
        harness.passBothPriorities(); // Resolve the draw trigger above the original graveyard trigger.
        harness.assertOnBattlefield(player1, "Masked Admirers");

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Masked Admirers"));
        harness.passBothPriorities(); // Resolve the mannequin sacrifice trigger.
        harness.passBothPriorities(); // Nameless Inversion has lost its target.
        harness.assertInGraveyard(player1, "Masked Admirers");

        harness.passBothPriorities(); // Resolve the original graveyard trigger's payment choice.
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(admirers.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(admirers.getId()));
    }
}
