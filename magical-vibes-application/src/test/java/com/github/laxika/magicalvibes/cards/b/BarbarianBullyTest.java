package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbarianBully.class, Forest.class})
class BarbarianBullyTest extends BaseCardTest {

    @Test
    void allPlayersDeclineAndBullyGetsBoosted() {
        Permanent bully = activateWithHand(new Forest());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(bully.getPowerModifier()).isEqualTo(2);
        assertThat(bully.getToughnessModifier()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void firstPlayerToAcceptTakesDamageAndPreventsBoost() {
        Permanent bully = activateWithHand(new Forest());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(bully.getPowerModifier()).isZero();
        assertThat(bully.getToughnessModifier()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canActivateOnlyOnceEachTurn() {
        activateWithHand(new Forest());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent activateWithHand(Forest cardInHand) {
        Permanent bully = harness.addToBattlefieldAndReturn(player1, new BarbarianBully());
        harness.setHand(player1, List.of(cardInHand));
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return bully;
    }
}
