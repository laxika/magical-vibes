package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamblingCieth.class, Spellbook.class, GrizzlyBears.class})
class ShamblingCiethTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new ShamblingCieth(), "{2}{B}");
        harness.passBothPriorities();

        Permanent cieth = findPermanent(player1, "Shambling Cie'th");
        assertThat(cieth.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A noncreature spell creates a may-pay trigger from the graveyard")
    void noncreatureSpellCreatesMayPayTrigger() {
        ShamblingCieth cieth = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(cieth));
        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{B}");
    }

    @Test
    @DisplayName("Paying {B} returns it from the graveyard to hand")
    void payingReturnsToHand() {
        ShamblingCieth cieth = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(cieth));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(cieth.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(cieth.getId()));
    }

    @Test
    @DisplayName("Declining the payment keeps it in the graveyard")
    void decliningKeepsItInGraveyard() {
        ShamblingCieth cieth = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(cieth));
        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(cieth.getId()));
    }

    @Test
    @DisplayName("A creature spell does not create the graveyard trigger")
    void creatureSpellDoesNotTrigger() {
        ShamblingCieth cieth = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(cieth));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(cieth.getId()));
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new ShamblingCieth()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Spellbook(), "{0}");

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Shambling Cie'th");
    }

    @Test
    void battlefieldCopyDoesNotTrigger() {
        harness.addToBattlefield(player1, new ShamblingCieth());

        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Shambling Cie'th");
    }

    @Test
    void cannotReturnWithoutBlackMana() {
        harness.setGraveyard(player1, List.of(new ShamblingCieth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Shambling Cie'th");
        harness.assertNotInHand(player1, "Shambling Cie'th");
    }

    @Test
    void paymentIsSpentAndCardReturnsBeforeSpellResolves() {
        harness.setGraveyard(player1, List.of(new ShamblingCieth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromHand(player1, new Spellbook(), "{0}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Shambling Cie'th");
        harness.assertNotInGraveyard(player1, "Shambling Cie'th");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void eachCopyRequiresItsOwnPayment() {
        ShamblingCieth first = new ShamblingCieth();
        ShamblingCieth second = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromHand(player1, new Spellbook(), "{0}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof ShamblingCieth).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof ShamblingCieth).hasSize(1);
    }

    @Test
    void oldTriggerCannotReturnCardThatLeftAndReenteredGraveyard() {
        ShamblingCieth cieth = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(cieth));
        gd.markGraveyardEntry(cieth);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromHand(player1, new Spellbook(), "{0}");

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(cieth));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(cieth));
        gd.markGraveyardEntry(cieth);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Shambling Cie'th");
        harness.assertNotInHand(player1, "Shambling Cie'th");
    }
}
