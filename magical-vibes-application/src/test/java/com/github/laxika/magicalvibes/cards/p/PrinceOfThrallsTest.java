package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Infest;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SavageHunger;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrinceOfThralls.class, GrizzlyBears.class, Shock.class, Naturalize.class, FountainOfYouth.class,
        Infest.class, SavageHunger.class, CrawWurm.class})
class PrinceOfThrallsTest extends BaseCardTest {

    /** player1 has the Prince and Shocks player2's Grizzly Bears; returns after both resolve. */
    private void princeAndShockGrizzly() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, dyingId);
        harness.passBothPriorities(); // Prince trigger resolves → the opponent is offered the choice
    }

    @Test
    @DisplayName("Opponent declines to pay — the permanent is put onto the battlefield under your control")
    void decliningStealsPermanent() {
        princeAndShockGrizzly();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent pays 3 life — the permanent stays in their graveyard")
    void payingKeepsPermanent() {
        princeAndShockGrizzly();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent who can't pay 3 life is stolen from without a choice")
    void cannotPayStealsAutomatically() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, dyingId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player2, 2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when the controller's own permanent is put into a graveyard")
    void doesNotTriggerForOwnPermanent() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, dyingId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Triggers for any permanent type — a destroyed opponent artifact can be stolen")
    void stealsNoncreaturePermanent() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities(); // Prince trigger resolves → the opponent is offered the choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Triggers when Prince and an opponent's creature die simultaneously")
    void triggersWhenPrinceDiesSimultaneously() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player2, new CrawWurm());
        UUID princeId = harness.getPermanentId(player1, "Prince of Thralls");
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Infest()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0, princeId);
        }
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Craw Wurm"));
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prince of Thralls");
        harness.assertInGraveyard(player2, "Craw Wurm");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertOnBattlefield(player1, "Craw Wurm");
        harness.assertNotInGraveyard(player2, "Craw Wurm");
    }

    @Test
    @DisplayName("A returned Aura lets the Prince's controller choose what it enchants")
    void returnedAuraChoosesAttachment() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SavageHunger()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castEnchantment(player2, 0, bearsId);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Savage Hunger"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID princeId = harness.getPermanentId(player1, "Prince of Thralls");
        harness.handlePermanentChosen(player1, princeId);
        harness.assertOnBattlefield(player1, "Savage Hunger");
        harness.assertNotInGraveyard(player2, "Savage Hunger");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Savage Hunger"))
                .singleElement().satisfies(permanent -> assertThat(permanent.getAttachedTo()).isEqualTo(princeId));
    }

    @Test
    @DisplayName("Each Prince offers a payment even after another Prince already returned the card")
    void secondTriggerStillOffersPaymentAfterCardLeavesGraveyard() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        princeAndShockGrizzly();
        harness.handleMayAbilityChosen(player2, false);
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
