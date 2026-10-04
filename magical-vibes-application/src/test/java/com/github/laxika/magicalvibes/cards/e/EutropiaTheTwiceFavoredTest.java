package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EutropiaTheTwiceFavored.class, GloriousAnthem.class, GrizzlyBears.class, Unsummon.class})
class EutropiaTheTwiceFavoredTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under its controller's control puts a counter on the chosen creature and gives it flying")
    void enchantmentEntryCountersAndGrantsFlying() {
        harness.addToBattlefield(player1, new EutropiaTheTwiceFavored());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGloriousAnthem();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying granted by the trigger wears off at end of turn while the counter remains")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new EutropiaTheTwiceFavored());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGloriousAnthem();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger ignores enchantments controlled by an opponent")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new EutropiaTheTwiceFavored());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger ignores non-enchantment permanents")
    void nonEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new EutropiaTheTwiceFavored());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Eutropia can target itself with its constellation ability")
    void canTargetItself() {
        Permanent eutropia = harness.addToBattlefieldAndReturn(player1, new EutropiaTheTwiceFavored());
        castGloriousAnthem();

        harness.handlePermanentChosen(player1, eutropia.getId());
        harness.passBothPriorities();

        assertThat(eutropia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, eutropia, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Each enchantment entry adds another counter to the chosen creature")
    void repeatedEntriesAccumulateCounters() {
        harness.addToBattlefield(player1, new EutropiaTheTwiceFavored());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        for (int i = 0; i < 2; i++) {
            castGloriousAnthem();
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities();
        }

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A pending constellation ability resolves after Eutropia leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent eutropia = harness.addToBattlefieldAndReturn(player1, new EutropiaTheTwiceFavored());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGloriousAnthem();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, eutropia.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(eutropia);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A constellation ability does not affect a creature that leaves and returns before resolution")
    void returnedCreatureIsANewTarget() {
        harness.addToBattlefield(player1, new EutropiaTheTwiceFavored());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGloriousAnthem();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        Permanent returnedBears = harness.addToBattlefieldAndReturn(
                player1, gd.playerHands.get(player1.getId()).removeFirst());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(returnedBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, returnedBears, Keyword.FLYING)).isFalse();
    }

    private void castGloriousAnthem() {
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }
}
