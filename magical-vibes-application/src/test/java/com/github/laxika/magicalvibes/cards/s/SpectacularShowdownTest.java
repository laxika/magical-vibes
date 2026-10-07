package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExaltedAngel;
import com.github.laxika.magicalvibes.cards.j.JungleShrine;
import com.github.laxika.magicalvibes.cards.t.Tatterkite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectacularShowdown.class, ExaltedAngel.class, Tatterkite.class,
        JungleShrine.class, SwordsToPlowshares.class})
class SpectacularShowdownTest extends BaseCardTest {

    @Test
    void targetedCastAddsCounterAndGoadsOnlyTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExaltedAngel());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ExaltedAngel());
        harness.setHand(player1, List.of(new SpectacularShowdown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.isGoaded(gd, target)).isTrue();
        assertThat(other.getCounterCount(CounterType.DOUBLE_STRIKE)).isZero();
        assertThat(gqs.isGoaded(gd, other)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void overloadAddsCountersAndGoadsOnlyCreaturesThatReceiveThem() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ExaltedAngel());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ExaltedAngel());
        Permanent creatureThatCantHaveCounters = harness.addToBattlefieldAndReturn(player2, new Tatterkite());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new JungleShrine());
        harness.setHand(player1, List.of(new SpectacularShowdown()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.isGoaded(gd, ownCreature)).isTrue();
        assertThat(gqs.isGoaded(gd, opposingCreature)).isTrue();
        assertThat(creatureThatCantHaveCounters.getCounterCount(CounterType.DOUBLE_STRIKE)).isZero();
        assertThat(gqs.isGoaded(gd, creatureThatCantHaveCounters)).isFalse();
        assertThat(land.getCounterCount(CounterType.DOUBLE_STRIKE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetedCreatureThatCannotReceiveCountersIsNotGoaded() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Tatterkite());
        harness.setHand(player1, List.of(new SpectacularShowdown()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.DOUBLE_STRIKE)).isZero();
        assertThat(gqs.isGoaded(gd, target)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spectacular Showdown");
    }

    @Test
    void existingDoubleStrikeCountersDoNotGoadUntargetedCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ExaltedAngel());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ExaltedAngel());
        target.setCounterCount(CounterType.DOUBLE_STRIKE, 1);
        other.setCounterCount(CounterType.DOUBLE_STRIKE, 1);
        harness.setHand(player1, List.of(new SpectacularShowdown()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(2);
        assertThat(gqs.isGoaded(gd, target)).isTrue();
        assertThat(other.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.isGoaded(gd, other)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void overloadCanResolveWithoutAnyCreatures() {
        harness.setHand(player1, List.of(new SpectacularShowdown()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spectacular Showdown");
    }

    @Test
    void removingTheTargetInResponseLeavesNoGoadAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExaltedAngel());
        harness.setHand(player1, List.of(new SpectacularShowdown()));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Exalted Angel");
        harness.assertInGraveyard(player1, "Spectacular Showdown");
        assertThat(target.getCounterCount(CounterType.DOUBLE_STRIKE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void overloadGoadExpiresOnCastersNextTurnButDoubleStrikeRemains() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ExaltedAngel());
        harness.setHand(player1, List.of(new SpectacularShowdown()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.isGoaded(gd, creature)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.isGoaded(gd, creature)).isFalse();
        assertThat(creature.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
