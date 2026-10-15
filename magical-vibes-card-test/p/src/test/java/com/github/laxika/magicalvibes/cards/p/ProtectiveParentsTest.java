package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeapingAmbush;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProtectiveParents.class, GrizzlyBears.class, GiantSpider.class, LeapingAmbush.class})
class ProtectiveParentsTest extends BaseCardTest {

    @Test
    void deathCreatesYoungHeroRoleAttachedToYourChosenCreature() {
        Permanent parents = addCreatureReady(player1, new ProtectiveParents());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        parents.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bear.getId()).doesNotContain(opposingBear.getId());

        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(bear.getId());
    }

    @Test
    void youngHeroPutsCounterOnAttachedCreatureWhenItAttacksWithToughnessAtMostThree() {
        Permanent bear = attachYoungHeroTo(addCreatureReady(player1, new GrizzlyBears()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void youngHeroDoesNotTriggerForAttachedCreatureWithToughnessAboveThree() {
        Permanent spider = attachYoungHeroTo(addCreatureReady(player1, new GiantSpider()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spider)));
        resolveAllTriggers();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathTriggerCanChooseNoCreature() {
        Permanent parents = addCreatureReady(player1, new ProtectiveParents());
        parents.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
    }

    @Test
    void youngHeroTriggersAtExactlyThreeToughness() {
        Permanent creature = attachYoungHeroTo(addCreatureReady(player1, new ProtectiveParents()));
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void youngHeroChecksToughnessAgainWhenAttackTriggerResolves() {
        Permanent creature = attachYoungHeroTo(addCreatureReady(player1, new ProtectiveParents()));
        harness.setHand(player1, List.of(new LeapingAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature))));
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void newestYoungHeroReplacesOlderRoleControlledBySamePlayer() {
        Permanent creature = attachYoungHeroTo(addCreatureReady(player1, new ProtectiveParents()));
        Permanent olderRole = findPermanent(player1, "Young Hero");

        attachYoungHeroTo(creature);

        assertThat(findPermanents(player1, "Young Hero")).hasSize(1);
        assertThat(findPermanent(player1, "Young Hero").getId()).isNotEqualTo(olderRole.getId());
        assertThat(findPermanent(player1, "Young Hero").getAttachedTo()).isEqualTo(creature.getId());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void youngHeroDoesNotGrantAttackAbilityToOtherCreatures() {
        Permanent enchanted = attachYoungHeroTo(addCreatureReady(player1, new ProtectiveParents()));
        Permanent other = addCreatureReady(player1, new ProtectiveParents());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(other)));
        resolveAllTriggers();

        assertThat(enchanted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent attachYoungHeroTo(Permanent creature) {
        Permanent parents = addCreatureReady(player1, new ProtectiveParents());
        parents.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        return creature;
    }
}
