package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FixerTechnoTerror.class, ChromaticStar.class, GrizzlyBears.class})
class FixerTechnoTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card after an artifact enters under its controller's control")
    void drawsAfterArtifactEntersUnderControl() {
        addFixer();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate without an artifact entering under its controller's control")
    void cannotActivateWithoutArtifactEntering() {
        addFixer();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
    }

    @Test
    @DisplayName("A nonartifact entering under its controller's control does not enable the ability")
    void nonartifactDoesNotEnableAbility() {
        addFixer();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
    }

    private Permanent addFixer() {
        return addCreatureReady(player1, new FixerTechnoTerror());
    }

    @Test
    void opponentsArtifactDoesNotEnableAbility() {
        addFixer();
        harness.enterBattlefieldAndReturn(player2, new ChromaticStar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
    }

    @Test
    void paysLifeAndTapsBeforeDrawing() {
        Permanent fixer = addFixer();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(fixer.isTapped()).isTrue();
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
    }

    @Test
    void cannotPayWithOnlyOneLife() {
        Permanent fixer = addFixer();
        harness.setLife(player1, 1);
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(fixer.isTapped()).isFalse();
        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void artifactEnteringBeforeFixerStillEnablesAbility() {
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());
        addFixer();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
    }

    @Test
    void artifactEntryDoesNotBypassSummoningSickness() {
        harness.enterBattlefieldAndReturn(player1, new FixerTechnoTerror());
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
