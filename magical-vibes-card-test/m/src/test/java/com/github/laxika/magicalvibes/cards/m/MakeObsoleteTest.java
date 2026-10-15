package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NightMarketLookout;
import com.github.laxika.magicalvibes.cards.e.EagerConstruct;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakeObsolete.class, EagerConstruct.class, NightMarketLookout.class})
class MakeObsoleteTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures opponents control -1/-1 and leaves your own creatures alone")
    void weakensOnlyOpponentCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new EagerConstruct());
        Permanent enemyBear = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());

        castMakeObsolete();

        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(enemyBear.getEffectivePower()).isEqualTo(1);
        assertThat(enemyBear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills an opponent's 1/1")
    void killsOneToughnessOpponentCreature() {
        harness.addToBattlefield(player2, new NightMarketLookout());

        castMakeObsolete();

        harness.assertNotOnBattlefield(player2, "Night Market Lookout");
        harness.assertInGraveyard(player2, "Night Market Lookout");
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent enemyBear = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());

        castMakeObsolete();

        assertThat(enemyBear.getEffectivePower()).isEqualTo(1);
        assertThat(enemyBear.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enemyBear.getEffectivePower()).isEqualTo(2);
        assertThat(enemyBear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void doesNotWeakenCreaturesEnteringLater() {
        castMakeObsolete();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new NightMarketLookout());

        assertThat(laterCreature.getEffectivePower()).isEqualTo(1);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Night Market Lookout");
    }

    @Test
    @DisplayName("Creatures entering while the spell is on the stack are affected")
    void includesCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new MakeObsolete()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0);
        harness.addToBattlefield(player2, new NightMarketLookout());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Night Market Lookout");
        harness.assertInGraveyard(player2, "Night Market Lookout");
    }

    @Test
    @DisplayName("Two copies combine their reductions")
    void reductionsFromMultipleSpellsAccumulate() {
        harness.addToBattlefield(player2, new EagerConstruct());

        castMakeObsolete();
        castMakeObsolete();

        harness.assertNotOnBattlefield(player2, "Eager Construct");
        harness.assertInGraveyard(player2, "Eager Construct");
    }

    private void castMakeObsolete() {
        harness.setHand(player1, List.of(new MakeObsolete()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0);
    }
}
