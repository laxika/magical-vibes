package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StitchedMangler.class, DevilthornFox.class})
class StitchedManglerTest extends BaseCardTest {

    @CardUsed({StitchedMangler.class, DevilthornFox.class})
    @Nested
    @DisplayName("Enters tapped")
    class EntersTapped {

        @Test
        @DisplayName("Enters the battlefield tapped")
        void entersTapped() {
            harness.addToBattlefield(player2, new DevilthornFox());
            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities(); // resolve creature spell

            Permanent mangler = findPermanent(player1, "Stitched Mangler");
            assertThat(mangler.isTapped()).isTrue();
        }

        @Test
        void canEnterTappedWithNoOpposingCreature() {
            harness.setHand(player1, List.of(new StitchedMangler()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.castCreature(player1, 0);
            harness.passBothPriorities();

            assertThat(findPermanent(player1, "Stitched Mangler").isTapped()).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }

    @CardUsed({StitchedMangler.class, DevilthornFox.class})
    @Nested
    @DisplayName("ETB trigger")
    class EnterTheBattlefield {

        @Test
        @DisplayName("ETB trigger goes on the stack when Stitched Mangler enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player2, new DevilthornFox());
            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Stitched Mangler");
        }

        @Test
        @DisplayName("Taps target creature an opponent controls")
        void tapsTargetCreature() {
            Permanent fox = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
            assertThat(fox.isTapped()).isFalse();

            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(fox.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Target creature doesn't untap during its controller's next untap step")
        void targetSkipsNextUntap() {
            Permanent fox = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());

            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(fox.isTapped()).isTrue();
            assertThat(fox.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        void restrictionExpiresAfterOneControllersUntapStep() {
            Permanent fox = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.performUntapStep(player1);
            assertThat(fox.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(fox.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(fox.isTapped()).isFalse();
        }

        @Test
        void alreadyTappedCreatureStillSkipsItsNextUntap() {
            Permanent fox = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
            fox.tap();
            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.performUntapStep(player2);
            assertThat(fox.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(fox.isTapped()).isFalse();
        }

        @Test
        void overlappingRestrictionsDoNotSkipTwoUntapSteps() {
            Permanent fox = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities();
            harness.passBothPriorities();
            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.performUntapStep(player2);
            assertThat(fox.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(fox.isTapped()).isFalse();
        }

        @Test
        void triggerStillResolvesAfterManglerLeavesBattlefield() {
            Permanent fox = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities();
            gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Stitched Mangler"));
            harness.passBothPriorities();

            assertThat(fox.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(fox.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(fox.isTapped()).isFalse();
        }

        @Test
        void targetBecomingControlledByManglerControllerMakesTriggerIllegal() {
            Permanent fox = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
            castMangler(player2, "Devilthorn Fox");
            harness.passBothPriorities();
            gd.playerBattlefields.get(player2.getId()).remove(fox);
            gd.playerBattlefields.get(player1.getId()).add(fox);
            harness.passBothPriorities();

            assertThat(fox.isTapped()).isFalse();
            fox.tap();
            harness.performUntapStep(player1);
            assertThat(fox.isTapped()).isFalse();
        }
    }

    @CardUsed({StitchedMangler.class, DevilthornFox.class})
    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new DevilthornFox());
            UUID ownFoxId = harness.getPermanentId(player1, "Devilthorn Fox");
            harness.setHand(player1, List.of(new StitchedMangler()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownFoxId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private void castMangler(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new StitchedMangler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
