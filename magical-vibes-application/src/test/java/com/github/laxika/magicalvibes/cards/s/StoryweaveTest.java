package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxWeaver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Storyweave.class, GrizzlyBears.class, NyxWeaver.class})
class StoryweaveTest extends BaseCardTest {

    @Test
    @DisplayName("The creature mode puts two +1/+1 counters on a creature you control")
    void creatureModePutsCountersOnControlledCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Saga mode puts lore counters on a Saga and prepares the next qualifying entry")
    void sagaModePutsLoreCountersAndPreparesEntry() {
        Permanent saga = addSagaTarget();
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 1, saga.getId());
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);

        Permanent creature = castNyxWeaver(player1);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The delayed replacement ignores non-enchantment creatures and is consumed once")
    void delayedReplacementOnlyAppliesToNextEnchantmentCreatureEntry() {
        Permanent saga = addSagaTarget();
        castStoryweaveSagaMode(saga);

        Permanent bears = castCreature(player1, new GrizzlyBears());
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent firstWeaver = castNyxWeaver(player1);
        Permanent secondWeaver = castNyxWeaver(player1);

        assertThat(firstWeaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondWeaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Storyweave modes enforce their target restrictions")
    void modesEnforceTargetRestrictions() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castStoryweaveSagaMode(Permanent saga) {
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, 1, saga.getId());
        harness.passBothPriorities();
    }

    private Permanent addSagaTarget() {
        Card sagaCard = new Card();
        sagaCard.setName("Test Saga");
        sagaCard.setType(CardType.ENCHANTMENT);
        sagaCard.setSubtypes(List.of(CardSubtype.SAGA));
        return harness.addToBattlefieldAndReturn(player1, sagaCard);
    }

    private Permanent castNyxWeaver(com.github.laxika.magicalvibes.model.Player player) {
        return castCreature(player, new NyxWeaver());
    }

    private Permanent castCreature(com.github.laxika.magicalvibes.model.Player player,
                                   com.github.laxika.magicalvibes.model.Card card) {
        harness.setHand(player, List.of(card));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .reduce((first, second) -> second)
                .orElseThrow();
    }
}
