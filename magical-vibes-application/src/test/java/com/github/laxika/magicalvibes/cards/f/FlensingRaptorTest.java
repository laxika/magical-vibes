package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlensingRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another toxic creature +1/+1 and flying until end of turn")
    void etbBoostsAndGrantsFlyingToToxicCreature() {
        Permanent toxicCreature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        castAndResolve(toxicCreature);

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, toxicCreature)).isEqualTo(2);
        assertThat(toxicCreature.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("The target must be a toxic creature you control")
    void cannotTargetNonToxicCreature() {
        Permanent nonToxicCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, nonToxicCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control with toxic");
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void boostAndFlyingWearOffAtEndOfTurn() {
        Permanent toxicCreature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        castAndResolve(toxicCreature);

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(2);
        assertThat(toxicCreature.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, toxicCreature)).isEqualTo(1);
        assertThat(toxicCreature.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("ETB fizzles if the toxic target leaves before resolution")
    void etbFizzlesIfTargetLeaves() {
        Permanent toxicCreature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, toxicCreature.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(toxicCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
