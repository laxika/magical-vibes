package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DauntlessAvenger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({BereavedSurvivor.class, DauntlessAvenger.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class})
class BereavedSurvivorTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when another creature you control dies")
    void transformsWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new BereavedSurvivor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        Permanent survivor = findPermanent(player1, "Bereaved Survivor");
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(survivor.isTransformed()).isTrue();
        assertThat(survivor.getCard().getName()).isEqualTo("Dauntless Avenger");
    }

    @Test
    @DisplayName("Returns a creature with mana value 2 or less tapped and attacking")
    void returnsSmallCreatureTappedAndAttacking() {
        Permanent avenger = addTransformedSurvivor();
        Card valid = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(valid));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(avenger.isAttackedThisTurn()).isTrue();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Does not offer a creature with mana value greater than 2")
    void doesNotOfferLargeCreature() {
        addTransformedSurvivor();
        Card invalid = new HillGiant();
        harness.setGraveyard(player1, List.of(invalid));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(invalid);
    }

    private Permanent addTransformedSurvivor() {
        BereavedSurvivor card = new BereavedSurvivor();
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    @Test
    @DisplayName("Multiple pending death triggers transform the survivor only once")
    void multiplePendingDeathTriggersTransformOnlyOnce() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new BereavedSurvivor());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(survivor.isTransformed()).isTrue();
        assertThat(survivor.getCard().getName()).isEqualTo("Dauntless Avenger");
    }

    @Test
    @DisplayName("An opponent's creature dying does not transform the survivor")
    void opponentCreatureDeathDoesNotTransform() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new BereavedSurvivor());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(survivor.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Dauntless Avenger does not transform back when an ally dies")
    void avengerDoesNotTransformOnLaterDeath() {
        Permanent avenger = addTransformedSurvivor();
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ally.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(avenger.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot return noncreatures or opponents' creatures")
    void doesNotOfferNoncreaturesOrOpponentGraveyard() {
        addTransformedSurvivor();
        Card noncreature = new LightningBolt();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
    }
}
