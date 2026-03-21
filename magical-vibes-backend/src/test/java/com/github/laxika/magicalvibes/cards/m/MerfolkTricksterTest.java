package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BlindingMage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

class MerfolkTricksterTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    class EnterTheBattlefield {

        @Test
        @DisplayName("ETB trigger goes on the stack when Merfolk Trickster enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castTrickster(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Merfolk Trickster");
        }

        @Test
        @DisplayName("Taps target creature an opponent controls")
        void tapsTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(bears.isTapped()).isFalse();

            castTrickster(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Target creature loses all keywords until end of turn")
        void targetLosesKeywords() {
            harness.addToBattlefield(player2, new AirElemental()); // has flying
            Permanent elemental = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();

            castTrickster(player2, "Air Elemental");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
        }

        @Test
        @DisplayName("Target creature loses activated abilities until end of turn")
        void targetLosesActivatedAbilities() {
            harness.addToBattlefield(player2, new BlindingMage());
            Permanent mage = gd.playerBattlefields.get(player2.getId()).getFirst();
            mage.setSummoningSick(false);

            castTrickster(player2, "Blinding Mage");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Blinding Mage has lost all abilities, so trying to activate should fail
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.forceActivePlayer(player2);
            harness.clearPriorityPassed();

            assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Merfolk Trickster enters the battlefield")
        void tricksterEntersBattlefield() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castTrickster(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            harness.assertOnBattlefield(player1, "Merfolk Trickster");
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
            harness.setHand(player1, List.of(new MerfolkTrickster()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("End of turn cleanup")
    class EndOfTurnCleanup {

        @Test
        @DisplayName("Ability loss wears off after resetModifiers")
        void abilityLossWearsOff() {
            harness.addToBattlefield(player2, new AirElemental()); // has flying
            Permanent elemental = gd.playerBattlefields.get(player2.getId()).getFirst();

            castTrickster(player2, "Air Elemental");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

            // Simulate end of turn cleanup
            elemental.resetModifiers();

            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        }
    }

    // ===== Helpers =====

    private void castTrickster(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new MerfolkTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
