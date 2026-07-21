package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StitchedManglerTest extends BaseCardTest {

    @Nested
    @DisplayName("Enters tapped")
    class EntersTapped {

        @Test
        @DisplayName("Enters the battlefield tapped")
        void entersTapped() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castMangler(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            Permanent mangler = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> "Stitched Mangler".equals(p.getCard().getName()))
                    .findFirst()
                    .orElseThrow();
            assertThat(mangler.isTapped()).isTrue();
        }
    }

    @Nested
    @DisplayName("ETB trigger")
    class EnterTheBattlefield {

        @Test
        @DisplayName("ETB trigger goes on the stack when Stitched Mangler enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castMangler(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Stitched Mangler");
        }

        @Test
        @DisplayName("Taps target creature an opponent controls")
        void tapsTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(bears.isTapped()).isFalse();

            castMangler(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Target creature doesn't untap during its controller's next untap step")
        void targetSkipsNextUntap() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            castMangler(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.setHand(player1, List.of(new StitchedMangler()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ===== Helpers =====

    private void castMangler(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new StitchedMangler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
