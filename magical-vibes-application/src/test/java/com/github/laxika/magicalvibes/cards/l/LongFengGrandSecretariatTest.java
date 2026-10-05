package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
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

@CardUsed({LongFengGrandSecretariat.class, Forest.class, GrizzlyBears.class, AshayaSoulOfTheWild.class})
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

    @Test
    @DisplayName("Long Feng's own death does not trigger when it is not a land")
    void ownDeathDoesNotTriggerWhenNotALand() {
        Permanent longFeng = harness.addToBattlefieldAndReturn(player1, new LongFengGrandSecretariat());
        harness.addToBattlefield(player1, new GrizzlyBears());

        putIntoGraveyard(longFeng);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A permanent that is both a creature and a land triggers only once")
    void creatureLandDeathTriggersOnlyOnce() {
        Permanent longFeng = harness.addToBattlefieldAndReturn(player1, new LongFengGrandSecretariat());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        Permanent dyingCreatureLand = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.isLand(gd, dyingCreatureLand)).isTrue();

        putIntoGraveyard(dyingCreatureLand);
        harness.handlePermanentChosen(player1, longFeng.getId());
        harness.passBothPriorities();

        assertThat(longFeng.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Long Feng's own death triggers when Ashaya makes it a land")
    void ownDeathTriggersWhenALand() {
        Permanent longFeng = harness.addToBattlefieldAndReturn(player1, new LongFengGrandSecretariat());
        Permanent ashaya = harness.addToBattlefieldAndReturn(player1, new AshayaSoulOfTheWild());
        assertThat(gqs.isLand(gd, longFeng)).isTrue();

        putIntoGraveyard(longFeng);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ashaya.getId()).doesNotContain(longFeng.getId());
        harness.handlePermanentChosen(player1, ashaya.getId());
        harness.passBothPriorities();

        assertThat(ashaya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
