package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneFlute.class, GoblinHero.class})
class BoneFluteTest extends BaseCardTest {

    @Test
    void activatedAbilityDebuffsAllCreaturesUntilEndOfTurn() {
        Permanent flute = harness.addToBattlefieldAndReturn(player1, new BoneFlute());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinHero());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(flute), null, null);
        harness.passBothPriorities();

        assertThat(flute.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(2);
    }

    @Test
    void creaturesEnteringAfterResolutionAreNotDebuffed() {
        Permanent flute = harness.addToBattlefieldAndReturn(player1, new BoneFlute());
        Permanent existingGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(flute), null, null);
        harness.passBothPriorities();

        Permanent laterGoblin = harness.enterBattlefieldAndReturn(player2, new GoblinHero());

        assertThat(gqs.getEffectivePower(gd, existingGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterGoblin)).isEqualTo(2);
    }
}
