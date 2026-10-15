package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlueDragon;
import com.github.laxika.magicalvibes.cards.b.BullsStrength;
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

@CardUsed({DragonsDisciple.class, BlueDragon.class, BullsStrength.class})
class DragonsDiscipleTest extends BaseCardTest {

    @Test
    void doesNotGetCounterWithoutDragonOrRevealableCard() {
        Permanent disciple = castDisciple(List.of(new DragonsDisciple()));

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayRevealDragonToEnterWithCounter() {
        Permanent disciple = castDisciple(List.of(new DragonsDisciple(), new BlueDragon()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningRevealLeavesDiscipleWithoutCounter() {
        Permanent disciple = castDisciple(List.of(new DragonsDisciple(), new BlueDragon()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void getsOnlyOneCounterWhenAlreadyControllingDragon() {
        harness.addToBattlefield(player1, new BlueDragon());

        Permanent disciple = castDisciple(List.of(new DragonsDisciple(), new BlueDragon()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void dragonsYouControlHaveWardOne() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new BlueDragon());
        harness.addToBattlefield(player1, new DragonsDisciple());
        castBullsStrengthAt(player2, dragon, 3);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Bull's Strength");
        assertThat(dragon.getPowerModifier()).isZero();
    }

    @Test
    void payingWardOneLetsTargetedSpellResolve() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new BlueDragon());
        harness.addToBattlefield(player1, new DragonsDisciple());
        castBullsStrengthAt(player2, dragon, 3);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void controllingDragonGetsCounterWithoutHandDragon() {
        harness.addToBattlefield(player1, new BlueDragon());

        Permanent disciple = castDisciple(List.of(new DragonsDisciple()));

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentDragonDoesNotGiveCounter() {
        harness.addToBattlefield(player2, new BlueDragon());

        Permanent disciple = castDisciple(List.of(new DragonsDisciple()));

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new BlueDragon());
        harness.addToBattlefield(player1, new DragonsDisciple());

        castBullsStrengthAt(player1, dragon, 3);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Bull's Strength");
        assertThat(dragon.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void wardTriggersAgainForSecondSpellInSameTurn() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new BlueDragon());
        harness.addToBattlefield(player1, new DragonsDisciple());
        castBullsStrengthAt(player2, dragon, 3);
        harness.handleMayAbilityChosen(player2, false);

        castBullsStrengthAt(player2, dragon, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void nonDragonDiscipleDoesNotHaveWard() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DragonsDisciple());

        castBullsStrengthAt(player2, disciple, 3);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(disciple.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void decliningRevealStillGetsCounterWhenControllingDragon() {
        harness.addToBattlefield(player1, new BlueDragon());

        Permanent disciple = castDisciple(List.of(new DragonsDisciple(), new BlueDragon()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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

    private void castBullsStrengthAt(Player player, Permanent target, int greenMana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new BullsStrength()));
        harness.addMana(player, ManaColor.GREEN, greenMana);
        harness.castAndResolveInstant(player, 0, target.getId());
    }
}
