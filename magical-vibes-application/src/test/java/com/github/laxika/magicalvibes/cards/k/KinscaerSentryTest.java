package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KinscaerSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature with mana value up to the attacker count onto the battlefield tapped and attacking")
    void putsCreatureWithManaValueUpToAttackerCountTappedAndAttacking() {
        Permanent sentry = addReadyCreature(new KinscaerSentry());
        addReadyCreature(new KnightOfMeadowgrain());
        GrizzlyBears bears = new GrizzlyBears();
        AirElemental elemental = new AirElemental();
        harness.setHand(player1, List.of(bears, elemental));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered).isNotNull();
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elemental);
    }

    @Test
    @DisplayName("Does not offer creatures above the number of attacking creatures")
    void doesNotOfferCreatureAboveAttackerCount() {
        addReadyCreature(new KinscaerSentry());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
