package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyCrushingMasterstroke.class, DarksteelIngot.class, GrizzlyBears.class, Forest.class})
class MyCrushingMasterstrokeTest extends BaseCardTest {

    @Test
    void stealsUntapsHastesAndForcesOpponentCreaturesToAttackTheirOwners() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.tap();
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        opponentArtifact.tap();
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        resolveScheme();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(opponentCreature, opponentArtifact, ownCreature)
                .doesNotContain(opponentLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentLand);
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentArtifact.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.HASTE)).isTrue();
        assertThat(opponentCreature.isMustAttackThisTurn()).isTrue();
        assertThat(opponentCreature.getMustAttackTargetId()).isEqualTo(player2.getId());
        assertThat(ownCreature.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void returnsPermanentsAndClearsTemporaryEffectsAtEndOfTurn() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        resolveScheme();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentCreature);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
        assertThat(opponentCreature.isMustAttackThisTurn()).isFalse();
        assertThat(opponentCreature.getMustAttackTargetId()).isNull();
    }

    private void resolveScheme() {
        MyCrushingMasterstroke scheme = new MyCrushingMasterstroke();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
