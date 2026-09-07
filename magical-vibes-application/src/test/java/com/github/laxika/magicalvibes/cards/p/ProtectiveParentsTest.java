package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProtectiveParents.class, GrizzlyBears.class, GiantSpider.class})
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
