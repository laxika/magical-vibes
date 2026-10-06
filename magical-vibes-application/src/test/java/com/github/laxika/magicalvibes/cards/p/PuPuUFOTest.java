package com.github.laxika.magicalvibes.cards.p;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GongagaReactorTown;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PuPuUFO.class, Forest.class, GongagaReactorTown.class})
class PuPuUFOTest extends BaseCardTest {

    @Test
    @DisplayName("PuPu UFO puts a land from hand onto the battlefield")
    void putsLandFromHandOntoBattlefield() {
        addReadyUfo();
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("PuPu UFO's second ability uses the number of Towns at resolution")
    void setsBasePowerToTownCountAtResolution() {
        Permanent ufo = addReadyUfo();
        addTowns(player1, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        addTowns(player1, 1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ufo)).isEqualTo(3);
    }

    @Test
    @DisplayName("PuPu UFO's base-power change wears off at end of turn")
    void basePowerChangeWearsOffAtEndOfTurn() {
        Permanent ufo = addReadyUfo();
        addTowns(player1, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ufo)).isEqualTo(3);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, ufo)).isNotEqualTo(3);
    }

    @Test
    void putsLandOntoBattlefieldAfterNormalLandPlay() {
        addReadyUfo();
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotPutNonlandCardOntoBattlefield() {
        Permanent ufo = addReadyUfo();
        PuPuUFO cardInHand = new PuPuUFO();
        harness.setHand(player1, List.of(cardInHand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ufo);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclinePuttingLandOntoBattlefield() {
        Permanent ufo = addReadyUfo();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ufo);
        assertThat(ufo.isTapped()).isTrue();
    }

    @Test
    void townPutOntoBattlefieldStillEntersTapped() {
        addReadyUfo();
        harness.setHand(player1, List.of(new GongagaReactorTown()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Gongaga, Reactor Town");
        Permanent town = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(town.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void townCountIsFixedAfterResolutionAndIgnoresOpponentsTowns() {
        Permanent ufo = addReadyUfo();
        addTowns(player1, 1);
        addTowns(player2, 3);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ufo)).isEqualTo(1);

        addTowns(player1, 2);
        assertThat(gqs.getEffectivePower(gd, ufo)).isEqualTo(1);
    }

    @Test
    void manaAbilityCanBeActivatedWhileSummoningSickAndTappedWithNoTowns() {
        Permanent ufo = harness.addToBattlefieldAndReturn(player1, new PuPuUFO());
        ufo.tap();
        ufo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ufo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ufo)).isEqualTo(5);
        assertThat(ufo.isTapped()).isTrue();
    }

    private Permanent addReadyUfo() {
        Permanent ufo = harness.addToBattlefieldAndReturn(player1, new PuPuUFO());
        ufo.setSummoningSick(false);
        return ufo;
    }

    private void addTowns(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new GongagaReactorTown());
        }
    }
}
