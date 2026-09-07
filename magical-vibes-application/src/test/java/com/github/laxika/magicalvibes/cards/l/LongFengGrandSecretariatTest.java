package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongFengGrandSecretariat.class, Forest.class, GrizzlyBears.class})
class LongFengGrandSecretariatTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control going to the graveyard lets you counter a creature you control")
    void ownCreatureGoingToGraveyardPutsCounterOnTargetCreature() {
        Permanent longFeng = harness.addToBattlefieldAndReturn(player1, new LongFengGrandSecretariat());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        putIntoGraveyard(dyingCreature);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(longFeng.getId(), recipient.getId())
                .doesNotContain(dyingCreature.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A land you control going to the graveyard triggers the ability")
    void ownLandGoingToGraveyardTriggers() {
        Permanent longFeng = harness.addToBattlefieldAndReturn(player1, new LongFengGrandSecretariat());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        putIntoGraveyard(land);
        harness.handlePermanentChosen(player1, longFeng.getId());
        harness.passBothPriorities();

        assertThat(longFeng.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature or land does not trigger the ability")
    void opponentPermanentGoingToGraveyardDoesNotTrigger() {
        harness.addToBattlefield(player1, new LongFengGrandSecretariat());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(opponentLand);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The triggered ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new LongFengGrandSecretariat());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        putIntoGraveyard(dyingCreature);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
