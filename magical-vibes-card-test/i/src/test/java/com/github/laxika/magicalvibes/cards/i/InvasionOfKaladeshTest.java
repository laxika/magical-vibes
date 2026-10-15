package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AetherwingGoldenScaleFlagship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfKaladesh.class, AetherwingGoldenScaleFlagship.class, GrizzlyBears.class, Spellbook.class})
class InvasionOfKaladeshTest extends BaseCardTest {

    @Test
    void entersWithDefenseAndCreatesThopter() {
        castInvasion();

        Permanent battle = findPermanent(player1, "Invasion of Kaladesh");
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(4);
        assertThat(battle.getProtectorPlayerId()).isEqualTo(player2.getId());
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
    }

    @Test
    void defeatedSiegeCastsBackFaceTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfKaladesh());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        resolveAllTriggers();

        Permanent aetherwing = findPermanent(player1, "Aetherwing, Golden-Scale Flagship");
        assertThat(aetherwing.isTransformed()).isTrue();
    }

    @Test
    void backFacePowerCountsArtifactsAndCrewAnimatesIt() {
        Permanent aetherwing = harness.addToBattlefieldAndReturn(
                player1, new AetherwingGoldenScaleFlagship());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, aetherwing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aetherwing)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, aetherwing)).isFalse();

        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, aetherwing)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void generatedThopterCanCrewFlagshipImmediately() {
        castInvasion();
        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(gqs.isArtifact(gd, thopter)).isTrue();
        assertThat(gqs.isCreature(gd, thopter)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);

        Permanent aetherwing = harness.addToBattlefieldAndReturn(
                player1, new AetherwingGoldenScaleFlagship());
        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        assertThat(thopter.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, aetherwing)).isTrue();
        assertThat(gqs.isArtifact(gd, aetherwing)).isTrue();
        assertThat(gqs.hasKeyword(gd, aetherwing, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, aetherwing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aetherwing)).isEqualTo(4);
    }

    @Test
    void powerTracksOnlyControlledArtifactsAfterCrewing() {
        Permanent aetherwing = harness.addToBattlefieldAndReturn(
                player1, new AetherwingGoldenScaleFlagship());
        harness.addToBattlefield(player2, new Spellbook());
        addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, aetherwing)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, aetherwing)).isEqualTo(1);

        harness.castFromHand(player1, new Spellbook(), "{0}");
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, aetherwing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aetherwing)).isEqualTo(4);
    }

    private void castInvasion() {
        harness.castFromHand(player1, new InvasionOfKaladesh(), "{U}{R}");
        resolveAllTriggers();
    }
}
