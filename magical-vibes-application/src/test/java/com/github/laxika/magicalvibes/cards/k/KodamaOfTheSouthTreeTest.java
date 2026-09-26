package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KodamaOfTheSouthTree.class, DesperateRitual.class, DevotedRetainer.class, HarshDeceiver.class})
class KodamaOfTheSouthTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell pumps other creatures you control and grants them trample")
    void arcaneSpellPumpsOtherCreatures() {
        addCreatureReady(player1, new KodamaOfTheSouthTree());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(retainer.getPowerModifier()).isEqualTo(1);
        assertThat(retainer.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, retainer, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Kodama does not pump itself")
    void kodamaDoesNotPumpItself() {
        Permanent kodama = addCreatureReady(player1, new KodamaOfTheSouthTree());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(kodama.getPowerModifier()).isZero();
        assertThat(kodama.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, kodama, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Casting a Spirit spell triggers the pump")
    void spiritSpellPumpsOtherCreatures() {
        addCreatureReady(player1, new KodamaOfTheSouthTree());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());

        harness.castFromHand(player1, new HarshDeceiver(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(retainer.getPowerModifier()).isEqualTo(1);
        assertThat(retainer.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, retainer, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures an opponent controls are unaffected")
    void opponentCreaturesUnaffected() {
        addCreatureReady(player1, new KodamaOfTheSouthTree());
        Permanent enemy = addCreatureReady(player2, new DevotedRetainer());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(enemy.getPowerModifier()).isZero();
        assertThat(enemy.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, enemy, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Spirit or Arcane spell does not trigger Kodama")
    void opponentSpiritOrArcaneSpellDoesNotTrigger() {
        addCreatureReady(player1, new KodamaOfTheSouthTree());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(retainer.getPowerModifier()).isZero();
        assertThat(retainer.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, retainer, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        addCreatureReady(player1, new KodamaOfTheSouthTree());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());

        harness.castFromHand(player1, new DevotedRetainer(), "{W}");
        harness.passBothPriorities();

        assertThat(retainer.getPowerModifier()).isZero();
        assertThat(retainer.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, retainer, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new KodamaOfTheSouthTree());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(retainer.getPowerModifier()).isZero();
        assertThat(retainer.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, retainer, Keyword.TRAMPLE)).isFalse();
    }
}
