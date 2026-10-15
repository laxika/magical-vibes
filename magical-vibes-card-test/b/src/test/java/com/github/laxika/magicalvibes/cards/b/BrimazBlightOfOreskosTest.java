package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrimazBlightOfOreskos.class, BasilicaShepherd.class, MyrSire.class, GrizzlyBears.class,
        DoublingSeason.class})
class BrimazBlightOfOreskosTest extends BaseCardTest {

    @Test
    void incubatesForTheManaValueOfAPhyrexianCreatureSpell() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.setHand(player1, List.of(new BasilicaShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator).isNotNull();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void incubatesForTheManaValueOfAnArtifactCreatureSpell() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.setHand(player1, List.of(new MyrSire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator).isNotNull();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotIncubateForAnUnqualifiedCreatureSpell() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void proliferatesAtEachEndStepAfterAPhyrexianDiesUnderYourControl() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.creatureSubtypeDeathCountThisTurn.put(
                player1.getId(), Map.of(CardSubtype.PHYREXIAN, 1));

        advanceToEndStep(player2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void doesNotProliferateWhenOnlyANonPhyrexianCreatureDied() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }

    @Test
    void eachDoubledIncubatorReceivesCounters() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setHand(player1, List.of(new MyrSire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator")).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(4));
    }

    @Test
    void incubatorTransformsAndRetainsItsCounters() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.setHand(player1, List.of(new MyrSire()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.passBothPriorities();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator),
                null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
    }

    @Test
    void doesNotIncubateForAnOpponentsQualifiedSpell() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MyrSire()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotProliferateForAnOpponentsPhyrexianDeath() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        gd.creatureSubtypeDeathCountThisTurn.put(player2.getId(), Map.of(CardSubtype.PHYREXIAN, 1));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferatesAfterAnActualPhyrexianDeath() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.addToBattlefield(player1, new BasilicaShepherd());
        Permanent shepherd = findPermanent(player1, "Basilica Shepherd");
        gd.playerPoisonCounters.put(player2.getId(), 1);

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, shepherd);
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }
}
