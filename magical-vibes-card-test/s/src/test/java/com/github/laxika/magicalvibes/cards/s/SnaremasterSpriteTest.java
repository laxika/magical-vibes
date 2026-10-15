package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnaremasterSprite.class})
class SnaremasterSpriteTest extends BaseCardTest {

    @Test
    void payingTwoManaTapsAnOpponentsCreatureAndPutsAStunCounterOnIt() {
        Permanent ownCreature = addCreatureReady(player1, new SnaremasterSprite());
        Permanent opponentCreature = addCreatureReady(player2, new SnaremasterSprite());
        castSpriteWithExtraMana();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds())
                .containsExactly(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void decliningTwoManaPaymentDoesNotTapOrStun() {
        Permanent opponentCreature = addCreatureReady(player2, new SnaremasterSprite());
        castSpriteWithExtraMana();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void reflexiveTriggerAllowsResponsesBeforeTappingAndStunning() {
        Permanent opponentCreature = addCreatureReady(player2, new SnaremasterSprite());
        castSpriteWithExtraMana();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void alreadyTappedCreatureStillReceivesStunCounter() {
        Permanent opponentCreature = addCreatureReady(player2, new SnaremasterSprite());
        opponentCreature.tap();
        castSpriteWithExtraMana();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void stunCounterReplacesNextUntapButNotFollowingUntap() {
        Permanent opponentCreature = addCreatureReady(player2, new SnaremasterSprite());
        castSpriteWithExtraMana();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        harness.performUntapStep(player2);

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);

        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void canCastAndDeclinePaymentWithoutOpposingCreatures() {
        castSpriteWithExtraMana();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Snaremaster Sprite");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castSpriteWithExtraMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new SnaremasterSprite(), "{U}");
        resolveAllTriggers();
    }
}
