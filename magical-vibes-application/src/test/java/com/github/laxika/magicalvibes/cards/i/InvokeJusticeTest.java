package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AutomatedArtificer;
import com.github.laxika.magicalvibes.cards.b.BruteSuit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvokeJustice.class, AutomatedArtificer.class, BruteSuit.class, IntercessorsArrest.class})
class InvokeJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent and distributes counters among the target player's creatures and Vehicles")
    void returnsPermanentAndDistributesCounters() {
        Card graveyardPermanent = new AutomatedArtificer();
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());
        Permanent targetVehicle = harness.addToBattlefieldAndReturn(player2, new BruteSuit());
        Permanent casterCreature = harness.addToBattlefieldAndReturn(player1, new AutomatedArtificer());
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, List.of(graveyardPermanent.getId(), player2.getId()));
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(graveyardPermanent.getId()));
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(targetVehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(casterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The returned permanent may receive all four counters")
    void returnedPermanentCanReceiveCounters() {
        Card graveyardPermanent = new AutomatedArtificer();
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, List.of(graveyardPermanent.getId(), player1.getId()));
        harness.handleXValueChosen(player1, 4);

        assertThat(findPermanent(player1, "Automated Artificer").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
    }

    @Test
    @DisplayName("Recipients with shroud may receive counters because they are not targets")
    void shroudDoesNotPreventDistribution() {
        Card graveyardPermanent = new AutomatedArtificer();
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());
        recipient.getGrantedKeywords().add(com.github.laxika.magicalvibes.model.Keyword.SHROUD);
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, List.of(graveyardPermanent.getId(), player2.getId()));
        harness.handleXValueChosen(player1, 4);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("A targeted player with no eligible permanents receives no counters")
    void noEligibleRecipientsStillReturnsPermanent() {
        Card graveyardPermanent = new AutomatedArtificer();
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, List.of(graveyardPermanent.getId(), player2.getId()));

        harness.assertOnBattlefield(player1, "Automated Artificer");
        assertThat(findPermanent(player1, "Automated Artificer").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature can be excluded from the counter distribution")
    void canAssignZeroToARecipient() {
        Card graveyardPermanent = new AutomatedArtificer();
        Permanent excluded = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new BruteSuit());
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, List.of(graveyardPermanent.getId(), player2.getId()));
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player1, 4);

        assertThat(excluded.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Counters are still distributed when the graveyard target becomes illegal")
    void illegalGraveyardTargetDoesNotPreventDistribution() {
        Card graveyardPermanent = new AutomatedArtificer();
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new BruteSuit());
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, List.of(graveyardPermanent.getId(), player2.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 4);

        harness.assertNotOnBattlefield(player1, "Automated Artificer");
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A returned Aura enters attached to a permanent chosen during resolution")
    void returnedAuraChoosesAttachment() {
        Card aura = new IntercessorsArrest();
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, List.of(aura.getId(), player2.getId()));
        harness.handlePermanentChosen(player1, enchanted.getId());
        harness.handleXValueChosen(player1, 4);

        harness.assertOnBattlefield(player1, "Intercessor's Arrest");
        assertThat(findPermanent(player1, "Intercessor's Arrest").getAttachedTo()).isEqualTo(enchanted.getId());
        assertThat(enchanted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertNotInGraveyard(player1, "Intercessor's Arrest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An Aura remains in the graveyard if there is nothing it can enchant")
    void auraWithoutLegalAttachmentRemainsInGraveyard() {
        Card aura = new IntercessorsArrest();
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, List.of(aura.getId(), player2.getId()));

        harness.assertInGraveyard(player1, "Intercessor's Arrest");
        harness.assertNotOnBattlefield(player1, "Intercessor's Arrest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
