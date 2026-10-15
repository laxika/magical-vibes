package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowstormVizier.class, Censor.class, DuneBeetle.class, TormentingVoice.class})
class ShadowstormVizierTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives this creature +1/+1")
    void cyclingBoostsSelf() {
        harness.addToBattlefield(player1, new ShadowstormVizier());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        assertThat(getVizier().getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities(); // resolve the boost trigger

        Permanent vizier = getVizier();
        assertThat(vizier.getPowerModifier()).isEqualTo(1);
        assertThat(vizier.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(vizier.getPowerModifier()).isEqualTo(1);
        assertThat(vizier.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each discard stacks another +1/+1")
    void discardsStack() {
        harness.addToBattlefield(player1, new ShadowstormVizier());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle(), new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent vizier = getVizier();
        assertThat(vizier.getPowerModifier()).isEqualTo(2);
        assertThat(vizier.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ShadowstormVizier());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent vizier = getVizier();
        assertThat(vizier.getPowerModifier()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(vizier.getPowerModifier()).isEqualTo(0);
        assertThat(vizier.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Discarding a noncycling card to cast a spell boosts the Vizier before the spell resolves")
    void ordinaryDiscardBoostsBeforeSpellResolves() {
        harness.addToBattlefield(player1, new ShadowstormVizier());
        harness.setHand(player1, List.of(new TormentingVoice(), new DuneBeetle()));
        harness.setLibrary(player1, List.of(new DuneBeetle(), new DuneBeetle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);

        assertThat(getVizier().getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(getVizier().getPowerModifier()).isEqualTo(1);
        assertThat(getVizier().getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Each Vizier receives its own boost from one discard")
    void multipleViziersEachBoostThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ShadowstormVizier());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ShadowstormVizier());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent cycling does not boost the Vizier")
    void opponentCyclingDoesNotBoost() {
        harness.addToBattlefield(player1, new ShadowstormVizier());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new DuneBeetle()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(getVizier().getPowerModifier()).isZero();
        assertThat(getVizier().getToughnessModifier()).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent getVizier() {
        return findPermanent(player1, "Shadowstorm Vizier");
    }
}
