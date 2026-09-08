package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IrreverentRevelers.class, FountainOfYouth.class, GrizzlyBears.class})
class IrreverentRevelersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode destroys target artifact")
    void destroysArtifactMode() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = gd.playerBattlefields.get(player2.getId()).getLast();

        castRevelers(0, artifact.getId());
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Destroy artifact mode rejects a creature target")
    void destroyModeRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getLast();

        prepareRevelers();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("ETB mode gives Irreverent Revelers haste until end of turn")
    void hasteMode() {
        castRevelers(1, null);
        resolveCreatureAndEtb();

        Permanent revelers = findPermanent(player1, "Irreverent Revelers");
        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    private void castRevelers(int mode, java.util.UUID targetId) {
        prepareRevelers();
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

    private void prepareRevelers() {
        harness.setHand(player1, List.of(new IrreverentRevelers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
