package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AetherwingGoldenScaleFlagship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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

    private void castInvasion() {
        harness.setHand(player1, List.of(new InvasionOfKaladesh()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
