package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IrradiateTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -1/-1 for each artifact you control")
    void shrinksForEachControlledArtifact() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Irradiate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, 2, giantId);
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Hill Giant"))
                .findFirst()
                .orElseThrow();
        assertThat(giant.getEffectivePower()).isEqualTo(1);
        assertThat(giant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at cleanup")
    void shrinkWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Irradiate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, 2, giantId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Irradiate cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Irradiate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, forestId))
                .isInstanceOf(IllegalStateException.class);
    }
}
