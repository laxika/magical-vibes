package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BridledBighorn.class, GrizzlyBears.class})
class BridledBighornTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Bridled Bighorn")
    void saddleTapsAnotherCreature() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bighorn.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled creates a Sheep token")
    void attacksWhileSaddledCreatesSheep() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());
        bighorn.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sheep")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not create a Sheep token")
    void doesNotCreateSheepWhenNotSaddled() {
        addCreatureReady(player1, new BridledBighorn());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sheep")).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());

        declareAttackers(player1, List.of(0));
        bighorn.setSaddled(true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sheep")).isEmpty();
    }

    @Test
    void canSaddleWithSummoningSickCreaturesWithoutTappingTheMount() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());
        bighorn.setSummoningSick(true);
        Permanent helper = addCreatureReady(player1, new BridledBighorn());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(helper.isTapped()).isTrue();
        assertThat(bighorn.isTapped()).isFalse();
        assertThat(bighorn.isSaddled()).isFalse();

        harness.passBothPriorities();

        assertThat(bighorn.isSaddled()).isTrue();
    }

    @Test
    void cannotUseTheMountItselfOrAnOpponentsCreatureToSaddle() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());
        Permanent opponent = addCreatureReady(player2, new BridledBighorn());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bighorn.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isFalse();
        assertThat(bighorn.isSaddled()).isFalse();
    }

    @Test
    void cannotSaddleDuringCombat() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());
        Permanent helper = addCreatureReady(player1, new BridledBighorn());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        assertThat(bighorn.isSaddled()).isFalse();
    }

    @Test
    void attackTriggerStillCreatesSheepAfterSourceLeavesBattlefield() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());
        bighorn.setSaddled(true);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(bighorn);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sheep")).hasSize(1);
        assertThat(findPermanents(player2, "Sheep")).isEmpty();
    }
}
