package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VivisPersistence.class, GrizzlyBears.class, Shock.class})
class VivisPersistenceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Wizard token that damages each opponent for noncreature spells")
    void createsWizardThatDamagesOpponentsForNoncreatureSpells() {
        harness.setHand(player1, List.of(new VivisPersistence(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player1, "Wizard");
        assertThat(wizard.getEffectivePower()).isZero();
        assertThat(wizard.getEffectiveToughness()).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The Wizard does not trigger for a creature spell")
    void wizardDoesNotTriggerForCreatureSpells() {
        harness.setHand(player1, List.of(new VivisPersistence(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A commander entering lets you pay to return Vivi's Persistence")
    void commanderEnteringReturnsCard() {
        VivisPersistence persistence = new VivisPersistence();
        harness.setGraveyard(player1, List.of(persistence));

        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        harness.setHand(player1, List.of(commander));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(persistence.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(persistence.getId()));
    }

    @Test
    @DisplayName("A commander attack lets you pay to return Vivi's Persistence")
    void commanderAttackingReturnsCard() {
        VivisPersistence persistence = new VivisPersistence();
        harness.setGraveyard(player1, List.of(persistence));

        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(persistence.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(persistence.getId()));
    }

    @Test
    @DisplayName("A noncommander attack does not trigger the graveyard ability")
    void noncommanderAttackDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new VivisPersistence()));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
