package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({AirElemental.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class,
        StoneBySunlight.class})
class StoneBySunlightTest extends BaseCardTest {

    @Test
    @DisplayName("Destroy mode destroys a target creature with power 4 or greater")
    void destroysLargeCreature() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        cast(0, target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Destroy mode cannot target a creature with power less than 4")
    void destroyModeRejectsSmallCreature() {
        Permanent target = addCreatureReady(player2, new HillGiant());

        assertThatThrownBy(() -> cast(0, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Protection mode adds the artifact type and indestructible until end of turn")
    void protectionModeAddsArtifactAndIndestructible() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        cast(1, target);

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Protection mode cannot target a noncreature permanent")
    void protectionModeRejectsNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThatThrownBy(() -> cast(1, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void cast(int modeIndex, Permanent target) {
        harness.setHand(player1, List.of(new StoneBySunlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, modeIndex, target.getId());
        harness.passBothPriorities();
    }
}
