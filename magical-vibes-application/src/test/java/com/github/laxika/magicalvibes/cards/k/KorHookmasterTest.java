package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorHookmaster.class, GrizzlyBears.class})
class KorHookmasterTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    @CardUsed({KorHookmaster.class, GrizzlyBears.class})
    class EnterTheBattlefield {

        @Test
        @DisplayName("Taps target creature an opponent controls")
        void tapsTargetCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            assertThat(bears.isTapped()).isFalse();

            castHookmaster(player2, "Grizzly Bears");
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Target creature doesn't untap during its controller's next untap step")
        void targetSkipsNextUntap() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            castHookmaster(player2, "Grizzly Bears");
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        void restrictionExpiresAfterOneControllerUntapStep() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new KorHookmaster());
            castHookmaster(player2, "Kor Hookmaster");
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.performUntapStep(player1);
            assertThat(target.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isFalse();
        }

        @Test
        void alreadyTappedCreatureStillSkipsUntap() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new KorHookmaster());
            target.setTapped(true);
            castHookmaster(player2, "Kor Hookmaster");
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isFalse();
        }

        @Test
        void twoTriggersBeforeUntapPreventOnlyTheNextUntap() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new KorHookmaster());
            castHookmaster(player2, "Kor Hookmaster");
            harness.passBothPriorities();
            harness.passBothPriorities();
            castHookmaster(player2, "Kor Hookmaster");
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isFalse();
        }

        @Test
        void triggerResolvesAfterSourceLeavesBattlefield() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new KorHookmaster());
            castHookmaster(player2, "Kor Hookmaster");
            harness.passBothPriorities();
            gd.playerBattlefields.get(player1.getId()).clear();
            harness.passBothPriorities();

            assertThat(target.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isFalse();
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    @CardUsed({KorHookmaster.class, GrizzlyBears.class})
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.setHand(player1, List.of(new KorHookmaster()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private void castHookmaster(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new KorHookmaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
