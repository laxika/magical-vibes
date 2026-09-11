package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonsDisciple.class, DragonHatchling.class, GiantGrowth.class})
class DragonsDiscipleTest extends BaseCardTest {

    @Test
    void doesNotGetCounterWithoutDragonOrRevealableCard() {
        Permanent disciple = castDisciple(List.of(new DragonsDisciple()));

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayRevealDragonToEnterWithCounter() {
        Permanent disciple = castDisciple(List.of(new DragonsDisciple(), new DragonHatchling()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningRevealLeavesDiscipleWithoutCounter() {
        Permanent disciple = castDisciple(List.of(new DragonsDisciple(), new DragonHatchling()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void getsOnlyOneCounterWhenAlreadyControllingDragon() {
        harness.addToBattlefield(player1, new DragonHatchling());

        Permanent disciple = castDisciple(List.of(new DragonsDisciple(), new DragonHatchling()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void dragonsYouControlHaveWardOne() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonHatchling());
        harness.addToBattlefield(player1, new DragonsDisciple());
        castGiantGrowthAt(player2, dragon, 2);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(dragon.getPowerModifier()).isZero();
    }

    @Test
    void payingWardOneLetsTargetedSpellResolve() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonHatchling());
        harness.addToBattlefield(player1, new DragonsDisciple());
        castGiantGrowthAt(player2, dragon, 2);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(3);
    }

    private Permanent castDisciple(List<? extends Card> hand) {
        harness.setHand(player1, List.copyOf(hand));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Dragon's Disciple");
    }

    private void castGiantGrowthAt(Player player, Permanent target, int greenMana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, greenMana);
        harness.castInstant(player, 0, target.getId());
        harness.passBothPriorities();
    }
}
