package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheNeutrinos.class, GrizzlyBears.class})
class TheNeutrinosTest extends BaseCardTest {

    @Test
    void allianceBoostsWhenAnotherCreatureEnters() {
        Permanent neutrinos = addCreatureReady(player1, new TheNeutrinos());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, neutrinos)).isEqualTo(3);
    }

    @Test
    void attackAbilityReturnsChosenCreatureTappedAndAttacking() {
        Permanent neutrinos = addCreatureReady(player1, new TheNeutrinos());
        Card targetCard = new GrizzlyBears();
        targetCard.setOwnerId(player1.getId());
        Permanent target = addCreatureReady(player1, targetCard);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isFalse();
        assertThat(findPermanent(player1, "The Neutrinos")).isSameAs(neutrinos);
    }

    @Test
    void attackAbilityCanResolveWithoutChoosingATarget() {
        addCreatureReady(player1, new TheNeutrinos());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Neutrinos");
    }
}
