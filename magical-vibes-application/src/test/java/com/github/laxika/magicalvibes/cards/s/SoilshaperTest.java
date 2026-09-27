package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Soilshaper.class, DesperateRitual.class, DevotedRetainer.class,
        Forest.class, HarshDeceiver.class})
class SoilshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell animates target land into a 3/3 that is still a land")
    void arcaneSpellAnimatesLand() {
        harness.addToBattlefield(player1, new Soilshaper());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Casting a Spirit spell animates target land")
    void spiritSpellAnimatesLand() {
        harness.addToBattlefield(player1, new Soilshaper());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.castFromHand(player1, new HarshDeceiver(), "{3}{W}");
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        harness.addToBattlefield(player1, new Soilshaper());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Casting an Arcane spell can animate an opponent's land")
    void arcaneSpellAnimatesOpponentsLand() {
        harness.addToBattlefield(player1, new Soilshaper());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.handlePermanentChosen(player1, opponentForest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opponentForest)).isTrue();
        assertThat(gqs.isLand(gd, opponentForest)).isTrue();
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Soilshaper());
        harness.addToBattlefield(player1, new Forest());

        harness.castFromHand(player1, new DevotedRetainer(), "{W}");

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A matching spell cast by an opponent does not trigger")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Soilshaper());
        harness.addToBattlefield(player1, new Forest());

        harness.castFromHand(player2, new DesperateRitual(), "{1}{R}");

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
