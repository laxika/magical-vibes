package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.o.OonasProwler;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SummonTheSchool.class, DeeptreadMerrow.class, GoldmeadowStalwart.class, OonasProwler.class})
class SummonTheSchoolTest extends BaseCardTest {

    @Nested
    @DisplayName("Spell effect")
    @CardUsed({SummonTheSchool.class})
    class SpellEffect {

        @Test
        @DisplayName("Creates two 1/1 blue Merfolk Wizard creature tokens")
        void createsTwoMerfolkWizardTokens() {
            harness.forceActivePlayer(player1);
            harness.setHand(player1, List.of(new SummonTheSchool()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castAndResolveSorcery(player1, 0, 0);

            List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken())
                    .toList();
            assertThat(tokens).hasSize(2);

            Permanent token = tokens.getFirst();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(token.getCard().getSubtypes())
                    .contains(CardSubtype.MERFOLK, CardSubtype.WIZARD);
        }
    }

    @Nested
    @DisplayName("Graveyard activated ability")
    @CardUsed({SummonTheSchool.class, DeeptreadMerrow.class, GoldmeadowStalwart.class, OonasProwler.class})
    class GraveyardAbility {

        @Test
        @DisplayName("Resolving returns Summon the School from graveyard to hand")
        void resolvingReturnsToHand() {
            SummonTheSchool card = new SummonTheSchool();
            harness.setGraveyard(player1, List.of(card));
            addMerfolk(player1, 4);

            harness.activateGraveyardAbility(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

            harness.passBothPriorities();

            harness.assertInHand(player1, "Summon the School");
            harness.assertNotInGraveyard(player1, "Summon the School");
        }

        @Test
        @DisplayName("Taps four Merfolk as the cost")
        void tapsFourMerfolkAsCost() {
            SummonTheSchool card = new SummonTheSchool();
            harness.setGraveyard(player1, List.of(card));
            addMerfolk(player1, 4);

            harness.activateGraveyardAbility(player1, 0);

            long tapped = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.MERFOLK))
                    .filter(Permanent::isTapped)
                    .count();
            assertThat(tapped).isEqualTo(4);
        }

        @Test
        @DisplayName("Cannot activate with fewer than four untapped Merfolk")
        void cannotActivateWithFewerThanFour() {
            SummonTheSchool card = new SummonTheSchool();
            harness.setGraveyard(player1, List.of(card));
            addMerfolk(player1, 3);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot activate when one of the Merfolk is already tapped")
        void cannotActivateWithTappedMerfolk() {
            SummonTheSchool card = new SummonTheSchool();
            harness.setGraveyard(player1, List.of(card));
            addMerfolk(player1, 4);
            gd.playerBattlefields.get(player1.getId()).getFirst().tap();

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot use a non-Merfolk permanent to pay the cost")
        void cannotActivateWithNonMerfolk() {
            SummonTheSchool card = new SummonTheSchool();
            harness.setGraveyard(player1, List.of(card));
            addMerfolk(player1, 3);
            addCreatureReady(player1, new GoldmeadowStalwart());

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot use Merfolk controlled by another player to pay the cost")
        void cannotActivateWithOpponentsMerfolk() {
            SummonTheSchool card = new SummonTheSchool();
            harness.setGraveyard(player1, List.of(card));
            addMerfolk(player1, 3);
            addMerfolk(player2, 1);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Newly entered Merfolk can pay the tap cost")
        void canTapSummoningSickMerfolk() {
            harness.setGraveyard(player1, List.of(new SummonTheSchool()));
            for (int i = 0; i < 4; i++) {
                harness.addToBattlefield(player1, new DeeptreadMerrow());
            }

            harness.activateGraveyardAbility(player1, 0);

            assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
            harness.passBothPriorities();
            harness.assertInHand(player1, "Summon the School");
        }

        @Test
        @DisplayName("An older activation cannot return the card after it leaves and reenters the graveyard")
        void cannotReturnNewGraveyardObject() {
            harness.setHand(player1, List.of());
            SummonTheSchool card = new SummonTheSchool();
            harness.setGraveyard(player1, List.of(card));
            harness.addToBattlefield(player1, new OonasProwler());
            addMerfolk(player1, 8);
            List<Permanent> merfolk = findPermanents(player1, "Deeptread Merrow");

            harness.activateGraveyardAbility(player1, 0);
            for (int i = 0; i < 4; i++) {
                harness.handlePermanentChosen(player1, merfolk.get(i).getId());
            }
            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();
            harness.assertInHand(player1, "Summon the School");
            assertThat(gd.stack).hasSize(1);

            harness.activateAbility(player1, 0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertNotInHand(player1, "Summon the School");
            harness.assertInGraveyard(player1, "Summon the School");
        }

        @Test
        @DisplayName("Returns only the graveyard copy whose ability was activated")
        void returnsOnlyActivatedCopy() {
            SummonTheSchool activatedCard = new SummonTheSchool();
            SummonTheSchool otherCard = new SummonTheSchool();
            harness.setGraveyard(player1, List.of(activatedCard, otherCard));
            addMerfolk(player1, 4);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId()))
                    .contains(activatedCard)
                    .doesNotContain(otherCard);
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCard);
        }
    }

    private void addMerfolk(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new DeeptreadMerrow());
        }
    }
}
