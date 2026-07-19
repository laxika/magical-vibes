package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

class ViewFromAboveTest extends BaseCardTest {

    private UUID castOnBears() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ViewFromAbove()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bears);
        return bears;
    }

    private Permanent bears(UUID bearsId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
    }

    // ===== Grants flying =====

    @Test
    @DisplayName("Target creature gains flying until end of turn")
    void grantsFlying() {
        UUID bearsId = castOnBears();
        harness.passBothPriorities();

        assertThat(bears(bearsId).getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOff() {
        UUID bearsId = castOnBears();
        harness.passBothPriorities();
        assertThat(bears(bearsId).getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears(bearsId).getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    // ===== Conditional self-return =====

    @Test
    @DisplayName("Without a white permanent, the spell goes to the graveyard")
    void noWhitePermanentGoesToGraveyard() {
        castOnBears();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("View from Above"));
    }

    @Test
    @DisplayName("Controlling a white permanent returns the spell to its owner's hand")
    void whitePermanentReturnsToHand() {
        harness.addToBattlefield(player1, new EliteVanguard());
        UUID bearsId = castOnBears();
        harness.passBothPriorities();

        // Flying still resolved on the target.
        assertThat(bears(bearsId).getGrantedKeywords()).contains(Keyword.FLYING);
        // Spell bounced off the stack back to hand rather than the graveyard.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("View from Above"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("View from Above"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new ViewFromAbove()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID relic = harness.getPermanentId(player1, "Darksteel Relic");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, relic))
                .isInstanceOf(IllegalStateException.class);
    }
}
