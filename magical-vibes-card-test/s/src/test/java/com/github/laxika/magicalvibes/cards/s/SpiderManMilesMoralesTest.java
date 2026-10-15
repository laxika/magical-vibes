package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AmateurHero;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderManMilesMorales.class, AmateurHero.class})
class SpiderManMilesMoralesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts counters on other creatures you control and gives them trample")
    void entersAndEmpowersOtherCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AmateurHero());
        Permanent opponentCreature = addCreatureReady(player2, new AmateurHero());

        harness.setHand(player1, List.of(new SpiderManMilesMorales()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent spiderMan = findPermanent(player1, "Spider-Man, Miles Morales");
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(spiderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking puts counters on other creatures you control and gives them trample")
    void attackEmpowersOtherCreatures() {
        Permanent spiderMan = addCreatureReady(player1, new SpiderManMilesMorales());
        Permanent ownCreature = addCreatureReady(player1, new AmateurHero());
        Permanent opponentCreature = addCreatureReady(player2, new AmateurHero());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(spiderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger's trample grant wears off at end of turn")
    void attackTrampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SpiderManMilesMorales());
        Permanent ownCreature = addCreatureReady(player1, new AmateurHero());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering before the attack trigger resolves receive its benefits")
    void attackUsesCreaturesPresentAtResolution() {
        addCreatureReady(player1, new SpiderManMilesMorales());
        addCreatureReady(player2, new AmateurHero());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent lateCreature = addCreatureReady(player1, new AmateurHero());
        resolveAllTriggers();

        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after the attack trigger resolves do not receive its benefits")
    void attackDoesNotAffectCreaturesEnteringAfterResolution() {
        addCreatureReady(player1, new SpiderManMilesMorales());
        Permanent existingCreature = addCreatureReady(player1, new AmateurHero());
        addCreatureReady(player2, new AmateurHero());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        Permanent lateCreature = addCreatureReady(player1, new AmateurHero());

        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The enters trigger's trample expires while its counters remain")
    void entersTrampleWearsOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new AmateurHero());
        harness.setHand(player1, List.of(new SpiderManMilesMorales()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
