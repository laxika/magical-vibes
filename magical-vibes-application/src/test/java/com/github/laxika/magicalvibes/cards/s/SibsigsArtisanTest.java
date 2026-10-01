package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SibsigsArtisan.class, GrizzlyBears.class, Mountain.class})
class SibsigsArtisanTest extends BaseCardTest {

    @Test
    void renewPutsThreeCountersOnTargetAndExilesSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card artisan = new SibsigsArtisan();
        harness.setGraveyard(player1, List.of(artisan));
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artisan);
    }

    @Test
    void renewPerpetuallyGrantsTheAbilityToTheTargetCreatureCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card targetCard = target.getCard();
        Card artisan = new SibsigsArtisan();
        harness.setGraveyard(player1, List.of(artisan));
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(targetCard.getId()));
    }

    @Test
    void renewOnlyTargetsCreaturesYouControl() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card artisan = new SibsigsArtisan();
        harness.setGraveyard(player1, List.of(artisan));
        readyRenew();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, ownLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void renewIsSorcerySpeedOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SibsigsArtisan()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyRenew() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
