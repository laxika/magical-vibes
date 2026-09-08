package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WildElephant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EbonyCharm.class, Forest.class, WildElephant.class})
class EbonyCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target opponent loses 1 life and you gain 1 life")
    class DrainMode {

        @Test
        @DisplayName("Drains 1 life from the targeted opponent")
        void drainsOneLife() {
            harness.setHand(player1, List.of(new EbonyCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        }

        @Test
        @DisplayName("Cannot target yourself")
        void cannotTargetSelf() {
            harness.setHand(player1, List.of(new EbonyCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player1.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Exile up to three target cards from a single graveyard")
    class ExileMode {

        @Test
        @DisplayName("Exiles three chosen cards from one graveyard")
        void exilesThreeCards() {
            Card a = new WildElephant();
            Card b = new WildElephant();
            Card c = new WildElephant();
            harness.setGraveyard(player2, List.of(a, b, c));
            harness.setHand(player1, List.of(new EbonyCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 1, null);
            List<UUID> targets = List.of(a.getId(), b.getId(), c.getId());
            harness.handleMultipleCardsChosen(player1, targets);
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(card -> targets.contains(card.getId()));
            assertThat(gd.exiledCards.stream().map(e -> e.card().getId()))
                    .contains(a.getId(), b.getId(), c.getId());
        }

        @Test
        @DisplayName("Choosing fewer than three leaves the rest in the graveyard")
        void choosingFewerLeavesRest() {
            Card chosen = new WildElephant();
            Card left = new WildElephant();
            harness.setGraveyard(player1, List.of(chosen, left));
            harness.setHand(player1, List.of(new EbonyCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 1, null);
            harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(card -> card.getId().equals(left.getId()))
                    .noneMatch(card -> card.getId().equals(chosen.getId()));
        }

        @Test
        @DisplayName("Choosing zero cards leaves the graveyard unchanged")
        void choosingZeroLeavesGraveyardUnchanged() {
            Card card = new WildElephant();
            harness.setGraveyard(player2, List.of(card));
            harness.setHand(player1, List.of(new EbonyCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 1, null);
            harness.handleMultipleCardsChosen(player1, List.of());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
            assertThat(gd.exiledCards)
                    .noneMatch(exiledCard -> exiledCard.card().getId().equals(card.getId()));
        }

        @Test
        @DisplayName("Targets must all come from a single graveyard")
        void rejectsTargetsAcrossTwoGraveyards() {
            Card mine = new WildElephant();
            Card theirs = new WildElephant();
            harness.setGraveyard(player1, List.of(mine));
            harness.setGraveyard(player2, List.of(theirs));
            harness.setHand(player1, List.of(new EbonyCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 1, null);

            assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(mine.getId(), theirs.getId())))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature gains fear until end of turn")
    class FearMode {

        @Test
        @DisplayName("Grants fear to the target creature")
        void grantsFear() {
            Permanent target = addCreatureReady(player1, new WildElephant());
            harness.setHand(player1, List.of(new EbonyCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();
        }

        @Test
        @DisplayName("Fear wears off at end of turn")
        void fearWearsOff() {
            Permanent target = addCreatureReady(player1, new WildElephant());
            harness.setHand(player1, List.of(new EbonyCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isFalse();
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreaturePermanent() {
            harness.addToBattlefield(player1, new Forest());
            harness.setHand(player1, List.of(new EbonyCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            Permanent forest = findPermanent(player1, "Forest");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, forest.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("creature");
        }
    }
}
