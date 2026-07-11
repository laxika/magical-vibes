package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SummonTheSchoolTest extends BaseCardTest {

    @Nested
    @DisplayName("Spell effect")
    class SpellEffect {

        @Test
        @DisplayName("Creates two 1/1 blue Merfolk Wizard creature tokens")
        void createsTwoMerfolkWizardTokens() {
            harness.forceActivePlayer(player1);
            harness.setHand(player1, List.of(new SummonTheSchool()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

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

            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Summon the School"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Summon the School"));
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
    }

    private void addMerfolk(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent merfolk = new Permanent(new ShaperApprentice());
            merfolk.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(merfolk);
        }
    }
}
