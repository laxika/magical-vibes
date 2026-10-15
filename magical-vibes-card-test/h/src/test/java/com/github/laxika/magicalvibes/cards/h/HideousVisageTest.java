package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.r.RustedSentinel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HideousVisage.class, RuneclawBear.class, GoblinPiker.class, RustedSentinel.class, Manalith.class})
class HideousVisageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Hideous Visage gives intimidate to each creature you control")
    void grantsIntimidateToOwnCreatures() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new HideousVisage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        for (Permanent own : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.hasKeyword(gd, own, Keyword.INTIMIDATE)).isTrue();
        }
        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Intimidate wears off at end of turn")
    void intimidateWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new HideousVisage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain intimidate")
    void laterCreaturesDoNotGainIntimidate() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new HideousVisage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addToBattlefield(player1, new RuneclawBear());

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(gqs.hasKeyword(gd, battlefield.getFirst(), Keyword.INTIMIDATE)).isTrue();
        assertThat(gqs.hasKeyword(gd, battlefield.getLast(), Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Granted intimidate allows same-color and artifact blockers but rejects other colors")
    void intimidateRestrictsBlockers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent greenBlocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent redBlocker = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());
        Permanent artifactBlocker = harness.addToBattlefieldAndReturn(player2, new RustedSentinel());
        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, redBlocker, attacker, defenders)).isTrue();
        harness.setHand(player1, List.of(new HideousVisage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bls.canBlockAttacker(gd, greenBlocker, attacker, defenders)).isTrue();
        assertThat(bls.canBlockAttacker(gd, artifactBlocker, attacker, defenders)).isTrue();
        assertThat(bls.canBlockAttacker(gd, redBlocker, attacker, defenders)).isFalse();
    }

    @Test
    @DisplayName("A colorless creature granted intimidate can only be blocked by artifact creatures")
    void colorlessAttackerRequiresArtifactBlocker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RustedSentinel());
        Permanent greenBlocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent artifactBlocker = harness.addToBattlefieldAndReturn(player2, new RustedSentinel());
        harness.setHand(player1, List.of(new HideousVisage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, greenBlocker, attacker, defenders)).isFalse();
        assertThat(bls.canBlockAttacker(gd, artifactBlocker, attacker, defenders)).isTrue();
    }

    @Test
    @DisplayName("Hideous Visage resolves without creatures and does not grant intimidate to noncreatures")
    void resolvesWithoutCreatures() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Manalith());
        harness.setHand(player1, List.of(new HideousVisage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INTIMIDATE)).isFalse();
        harness.assertInGraveyard(player1, "Hideous Visage");
        assertThat(gd.stack).isEmpty();
    }
}
