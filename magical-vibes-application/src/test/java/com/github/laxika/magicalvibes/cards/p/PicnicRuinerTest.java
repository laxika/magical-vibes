package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.cards.f.FerociousWerefox;
import com.github.laxika.magicalvibes.cards.s.StolenGoodies;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PicnicRuiner.class, StolenGoodies.class, FerociousWerefox.class, CandyGrapple.class})
class PicnicRuinerTest extends BaseCardTest {

    @Test
    void adventureDistributesThreeCountersAmongControlledCreatures() {
        Permanent first = addCreatureReady(player1, new PicnicRuiner());
        Permanent second = addCreatureReady(player1, new PicnicRuiner());
        PicnicRuiner card = new PicnicRuiner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, 0, Map.of(first.getId(), 1, second.getId(), 2));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCanBeCastWithNoTargets() {
        PicnicRuiner card = new PicnicRuiner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetAnOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new PicnicRuiner());
        harness.setHand(player1, List.of(new PicnicRuiner()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castAdventure(
                player1, 0, 0, Map.of(opponentCreature.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackingWithAControlledCreatureOfPowerFourGivesDoubleStrike() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());
        addCreatureReady(player1, new FerociousWerefox());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void attackDoesNotGiveDoubleStrikeWithoutAControlledCreatureOfPowerFour() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void doubleStrikeStillResolvesAfterTheQualifyingCreatureDies() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());
        Permanent werefox = addCreatureReady(player1, new FerociousWerefox());
        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, werefox.getId());
        harness.assertNotOnBattlefield(player1, "Ferocious Werefox");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void ruinerCanQualifyForItsOwnAttackTrigger() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());
        ruiner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void opponentsPowerFourCreatureDoesNotQualify() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());
        addCreatureReady(player2, new FerociousWerefox());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void gainingPowerFourAfterAttackingDoesNotCreateATrigger() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).isEmpty();
        ruiner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void adventureDoesNotRedistributeCountersFromADeadTarget() {
        Permanent first = addCreatureReady(player1, new PicnicRuiner());
        Permanent second = addCreatureReady(player1, new PicnicRuiner());
        PicnicRuiner card = new PicnicRuiner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAdventure(player1, 0, 0, Map.of(first.getId(), 1, second.getId(), 2));
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, first.getId())).isNull();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureWithAllTargetsGoneGoesToGraveyardInsteadOfExile() {
        Permanent target = addCreatureReady(player1, new PicnicRuiner());
        PicnicRuiner card = new PicnicRuiner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAdventure(player1, 0, 0, Map.of(target.getId(), 3));
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void adventureRejectsAnIncompleteCounterDistribution() {
        Permanent target = addCreatureReady(player1, new PicnicRuiner());
        harness.setHand(player1, List.of(new PicnicRuiner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, 0, Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureRejectsATargetAssignedZeroCounters() {
        Permanent first = addCreatureReady(player1, new PicnicRuiner());
        Permanent second = addCreatureReady(player1, new PicnicRuiner());
        harness.setHand(player1, List.of(new PicnicRuiner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castAdventure(
                player1, 0, 0, Map.of(first.getId(), 3, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureCanBeCastFromExileAfterATargetlessAdventure() {
        PicnicRuiner card = new PicnicRuiner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Picnic Ruiner");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());
        addCreatureReady(player1, new FerociousWerefox());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
