package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.f.FinishingBlow;
import com.github.laxika.magicalvibes.cards.n.NiambiEsteemedSpeaker;
import com.github.laxika.magicalvibes.cards.s.ShatterTheSky;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BasrisLieutenant.class, AlpineWatchdog.class, FinishingBlow.class})
class BasrisLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature you control")
    void etbPutsCounterOnTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new BasrisLieutenant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new BasrisLieutenant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Creates a vigilant Knight when an ally with a +1/+1 counter dies")
    void createsKnightWhenCounteredAllyDies() {
        harness.addToBattlefield(player1, new BasrisLieutenant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithFinishingBlow(player2, bears.getId());
        harness.passBothPriorities();

        Permanent knight = findPermanents(player1, "Knight").getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(knight.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("The Lieutenant creates a Knight when it dies with a +1/+1 counter")
    void createsKnightWhenItselfDiesWithCounter() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new BasrisLieutenant());
        lieutenant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithFinishingBlow(player2, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Knight when an ally dies without a +1/+1 counter")
    void doesNotCreateKnightWhenAllyHasNoPlusOnePlusOneCounter() {
        harness.addToBattlefield(player1, new BasrisLieutenant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());

        destroyWithFinishingBlow(player2, bears.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    @Test
    void vigilanceKeepsLieutenantUntappedWhenAttacking() {
        Permanent lieutenant = addCreatureReady(player1, new BasrisLieutenant());

        declareAttackers(List.of(0));

        assertThat(lieutenant.isTapped()).isFalse();
    }

    @Test
    @CardUsed({NiambiEsteemedSpeaker.class})
    void protectionPreventsTargetingByYourOwnMulticoloredAbility() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new BasrisLieutenant());
        harness.setHand(player1, List.of(new NiambiEsteemedSpeaker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.addToBattlefield(player1, new AlpineWatchdog());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, lieutenant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCreateKnightWhenItselfDiesWithoutCounter() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new BasrisLieutenant());

        destroyWithFinishingBlow(player2, lieutenant.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    @Test
    void doesNotTriggerForOpponentCreatureWithCounter() {
        harness.addToBattlefield(player1, new BasrisLieutenant());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithFinishingBlow(player1, creature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    @Test
    void multipleCountersStillCreateOnlyOneKnight() {
        harness.addToBattlefield(player1, new BasrisLieutenant());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        destroyWithFinishingBlow(player2, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
    }

    @Test
    @CardUsed({ShatterTheSky.class})
    void simultaneousDeathsCreateKnightForEachCounteredCreatureIncludingLieutenant() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new BasrisLieutenant());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        lieutenant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new AlpineWatchdog()));
        harness.setHand(player1, List.of(new ShatterTheSky()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
        harness.assertInGraveyard(player1, "Basri's Lieutenant");
        harness.assertInGraveyard(player1, "Alpine Watchdog");
    }

    private void destroyWithFinishingBlow(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new FinishingBlow()));
        harness.addMana(caster, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
