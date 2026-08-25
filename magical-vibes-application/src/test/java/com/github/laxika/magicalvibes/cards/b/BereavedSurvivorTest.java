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

        declareAttack();
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

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(invalid);
    }

    private Permanent addTransformedSurvivor() {
        BereavedSurvivor card = new BereavedSurvivor();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
