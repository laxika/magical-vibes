package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlphaDeathclaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NuclearFallout.class, GrizzlyBears.class, AlphaDeathclaw.class})
class NuclearFalloutTest extends BaseCardTest {

    @Test
    @DisplayName("Gives each player X rad counters and each creature -2X/-2X")
    void givesRadCountersAndWeakensAllCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2: {2}{B}{B}

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(ownBear.getPowerModifier()).isEqualTo(-4);
        assertThat(ownBear.getToughnessModifier()).isEqualTo(-4);
        assertThat(opposingBear.getPowerModifier()).isEqualTo(-4);
        assertThat(opposingBear.getToughnessModifier()).isEqualTo(-4);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("X=0 leaves creatures and existing rad counters unchanged")
    void zeroXDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlphaDeathclaw());
        gd.playerRadCounters.put(player1.getId(), 2);
        gd.playerRadCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Alpha Deathclaw");
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds rad counters even when there are no creatures")
    void addsRadCountersOnEmptyBattlefield() {
        gd.playerRadCounters.put(player1.getId(), 1);
        gd.playerRadCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(6);
        harness.assertInGraveyard(player1, "Nuclear Fallout");
    }

    @Test
    @DisplayName("Weakening expires at cleanup and does not affect later creatures")
    void weakeningExpiresAndDoesNotAffectLaterCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlphaDeathclaw());
        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new AlphaDeathclaw());
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UNTAP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures with zero toughness die on both battlefields")
    void killsCreaturesOnBothBattlefields() {
        harness.addToBattlefield(player1, new AlphaDeathclaw());
        harness.addToBattlefield(player2, new AlphaDeathclaw());
        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertNotOnBattlefield(player1, "Alpha Deathclaw");
        harness.assertNotOnBattlefield(player2, "Alpha Deathclaw");
        harness.assertInGraveyard(player1, "Alpha Deathclaw");
        harness.assertInGraveyard(player2, "Alpha Deathclaw");
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
    }
}
