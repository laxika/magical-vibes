package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GladehartCavalry;
import com.github.laxika.magicalvibes.cards.i.ImmobilizerEldrazi;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.w.WalkerOfTheWastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KozileksReturn.class, com.github.laxika.magicalvibes.cards.d.DeceiverOfForm.class,
        GrizzlyBears.class, SerraAngel.class, ImmobilizerEldrazi.class, WalkerOfTheWastes.class,
        GladehartCavalry.class})
class KozileksReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature without damaging players")
    void dealsTwoDamageToEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.setHand(player1, List.of(new KozileksReturn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("May exile from the graveyard for 5 damage when a large Eldrazi is cast")
    void mayExileFromGraveyardForFiveDamage() {
        KozileksReturn returnCard = new KozileksReturn();
        harness.setGraveyard(player1, List.of(returnCard));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.DeceiverOfForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Kozilek's Return");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Kozilek's Return"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deceiver of Form");
    }

    @Test
    @DisplayName("Declining the graveyard trigger leaves the card in the graveyard")
    void decliningGraveyardTriggerLeavesCard() {
        KozileksReturn returnCard = new KozileksReturn();
        harness.setGraveyard(player1, List.of(returnCard));
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.DeceiverOfForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Kozilek's Return");
    }

    @Test
    @DisplayName("The accepted graveyard ability kills four-toughness creatures on both sides")
    void graveyardDamageHitsBothPlayersCreatures() {
        harness.setGraveyard(player1, List.of(new KozileksReturn()));
        harness.addToBattlefield(player1, new WalkerOfTheWastes());
        harness.addToBattlefield(player2, new WalkerOfTheWastes());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.DeceiverOfForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Walker of the Wastes");
        harness.assertNotOnBattlefield(player2, "Walker of the Wastes");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deceiver of Form");
    }

    @Test
    @DisplayName("Declining the graveyard ability does not damage creatures")
    void decliningDoesNotDealDamage() {
        harness.setGraveyard(player1, List.of(new KozileksReturn()));
        harness.addToBattlefield(player2, new WalkerOfTheWastes());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.DeceiverOfForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Walker of the Wastes");
        harness.assertInGraveyard(player1, "Kozilek's Return");
    }

    @Test
    @DisplayName("Removing the source from the graveyard stops the pending ability")
    void absentSourceDoesNotDealDamage() {
        harness.setGraveyard(player1, List.of(new KozileksReturn()));
        harness.addToBattlefield(player2, new WalkerOfTheWastes());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.DeceiverOfForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Walker of the Wastes");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deceiver of Form");
    }

    @Test
    @DisplayName("An Eldrazi with mana value below seven does not trigger the graveyard ability")
    void smallEldraziDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new KozileksReturn()));
        harness.addToBattlefield(player2, new WalkerOfTheWastes());
        harness.setHand(player1, List.of(new ImmobilizerEldrazi()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Immobilizer Eldrazi");
        harness.assertOnBattlefield(player2, "Walker of the Wastes");
        harness.assertInGraveyard(player1, "Kozilek's Return");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A seven-mana creature without the Eldrazi subtype does not trigger the ability")
    void largeNonEldraziDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new KozileksReturn()));
        harness.setHand(player1, List.of(new GladehartCavalry()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Kozilek's Return");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent casting a large Eldrazi does not trigger your graveyard ability")
    void opponentsEldraziDoesNotTrigger() {
        harness.setGraveyard(player2, List.of(new KozileksReturn()));
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.DeceiverOfForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deceiver of Form");
        harness.assertInGraveyard(player2, "Kozilek's Return");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A graveyard trigger cannot damage creatures after its source leaves and returns")
    void returnedGraveyardObjectCannotPayForOldTrigger() {
        KozileksReturn returnCard = new KozileksReturn();
        harness.setGraveyard(player1, List.of(returnCard));
        harness.addToBattlefield(player2, new WalkerOfTheWastes());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.DeceiverOfForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);

        // Model the source being returned to hand while its original trigger is pending.
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(returnCard));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Walker of the Wastes");
        harness.assertInGraveyard(player1, "Kozilek's Return");

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertOnBattlefield(player2, "Walker of the Wastes");
        harness.assertInGraveyard(player1, "Kozilek's Return");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
