package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.cards.w.WandOfDenial;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuneralCharm.class, LongbowArcher.class, WandOfDenial.class})
class FuneralCharmTest extends BaseCardTest {

    @Nested
    @CardUsed({FuneralCharm.class, LongbowArcher.class, WandOfDenial.class})
    @DisplayName("Mode 0: Target player discards a card")
    class DiscardMode {

        @Test
        @DisplayName("The targeted player chooses which card to discard")
        void targetedPlayerChoosesDiscard() {
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.setHand(player2, List.of(new LongbowArcher(), new WandOfDenial()));

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();
            harness.handleCardChosen(player2, 1);

            harness.assertInHand(player2, "Longbow Archer");
            harness.assertNotInHand(player2, "Wand of Denial");
            harness.assertInGraveyard(player2, "Wand of Denial");
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        }

        @Test
        @DisplayName("Targeted player discards a card")
        void targetDiscards() {
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.setHand(player2, List.of(new LongbowArcher(), new LongbowArcher()));

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player2, 0);

            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        }

        @Test
        @DisplayName("Can target yourself")
        void canTargetSelf() {
            harness.setHand(player1, List.of(new FuneralCharm(), new LongbowArcher()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 0, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            harness.assertInGraveyard(player1, "Longbow Archer");
        }

        @Test
        @DisplayName("Can target a player with no cards in hand")
        void canTargetPlayerWithEmptyHand() {
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.setHand(player2, List.of());

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        }

        @Test
        @DisplayName("Cannot target a permanent")
        void cannotTargetPermanent() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new WandOfDenial());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({FuneralCharm.class, LongbowArcher.class, WandOfDenial.class})
    @DisplayName("Mode 1: Target creature gets +2/-1 until end of turn")
    class BoostMode {

        @Test
        @DisplayName("Repeated boosts put a creature with zero toughness into the graveyard")
        void repeatedBoostsKillCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new LongbowArcher());
            harness.setHand(player1, List.of(new FuneralCharm(), new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castInstant(player1, 0, 1, target.getId());
            harness.passBothPriorities();
            harness.assertOnBattlefield(player2, "Longbow Archer");

            harness.castInstant(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Longbow Archer");
            harness.assertInGraveyard(player2, "Longbow Archer");
        }

        @Test
        @DisplayName("Gives +2/-1")
        void boostsTarget() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            assertThat(target.getEffectivePower()).isEqualTo(4);
            assertThat(target.getEffectiveToughness()).isEqualTo(1);
        }

        @Test
        @DisplayName("Can target an opponent's creature")
        void boostsOpponentsCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new LongbowArcher());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            assertThat(target.getEffectivePower()).isEqualTo(4);
            assertThat(target.getEffectiveToughness()).isEqualTo(1);
        }

        @Test
        @DisplayName("Boost wears off at end of turn")
        void boostWearsOff() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            harness.passUntil(TurnStep.CLEANUP);

            assertThat(target.getEffectivePower()).isEqualTo(2);
            assertThat(target.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new WandOfDenial());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({FuneralCharm.class, LongbowArcher.class, WandOfDenial.class})
    @DisplayName("Mode 2: Target creature gains swampwalk until end of turn")
    class SwampwalkMode {

        @Test
        @DisplayName("Grants swampwalk")
        void grantsSwampwalk() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isTrue();
        }

        @Test
        @DisplayName("Can target an opponent's creature")
        void grantsSwampwalkToOpponentsCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new LongbowArcher());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isTrue();
        }

        @Test
        @DisplayName("Swampwalk wears off at end of turn")
        void swampwalkWearsOff() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();

            harness.passUntil(TurnStep.CLEANUP);

            assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isFalse();
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new WandOfDenial());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
