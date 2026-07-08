package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudgoatRangerTest extends BaseCardTest {

    // ===== ETB: creates three Kithkin Soldier tokens =====

    @Test
    @DisplayName("ETB creates three 1/1 white Kithkin Soldier tokens")
    void etbCreatesThreeKithkinSoldierTokens() {
        castAndResolveRanger();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(countKithkinSoldierTokens(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Kithkin Soldier tokens are 1/1 with Kithkin and Soldier subtypes")
    void kithkinSoldierTokensHaveCorrectStats() {
        castAndResolveRanger();

        Permanent token = findKithkinSoldierToken(player1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.KITHKIN, CardSubtype.SOLDIER);
    }

    // ===== Activated ability =====

    @Nested
    @DisplayName("Activated ability")
    class ActivatedAbilityTests {

        @Test
        @DisplayName("Activating ability puts it on the stack")
        void activatingPutsOnStack() {
            Permanent ranger = addRangerReady(player1);
            addKithkin(player1, 3);

            harness.activateAbility(player1, 0, null, null);
            tapKithkin(player1, 3);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Cloudgoat Ranger");
        }

        @Test
        @DisplayName("Resolving ability gives Cloudgoat Ranger +2/+0 and flying until end of turn")
        void resolvingGrantsBoostAndFlying() {
            Permanent ranger = addRangerReady(player1);
            addKithkin(player1, 3);

            harness.activateAbility(player1, 0, null, null);
            tapKithkin(player1, 3);
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, ranger, Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("Boost and flying wear off at end of turn cleanup")
        void boostAndFlyingWearOffAtEndOfTurn() {
            Permanent ranger = addRangerReady(player1);
            addKithkin(player1, 3);

            harness.activateAbility(player1, 0, null, null);
            tapKithkin(player1, 3);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, ranger, Keyword.FLYING)).isFalse();
        }

        @Test
        @DisplayName("Tapping three Kithkin taps them as cost")
        void tapsKithkinAsCost() {
            addRangerReady(player1);
            addKithkin(player1, 4);

            harness.activateAbility(player1, 0, null, null);
            tapKithkin(player1, 3);

            long tappedKithkin = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                    .filter(Permanent::isTapped)
                    .count();
            assertThat(tappedKithkin).isEqualTo(3);

            long untappedKithkin = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                    .filter(p -> !p.isTapped())
                    .count();
            assertThat(untappedKithkin).isEqualTo(1);
        }

        @Test
        @DisplayName("With exactly three Kithkin, auto-pays the tap cost")
        void autoPaysWithExactlyThreeKithkin() {
            addRangerReady(player1);
            addKithkin(player1, 3);

            harness.activateAbility(player1, 0, null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Cloudgoat Ranger");

            long tappedKithkin = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                    .filter(Permanent::isTapped)
                    .count();
            assertThat(tappedKithkin).isEqualTo(3);
        }

        @Test
        @DisplayName("Cannot activate with fewer than three untapped Kithkin")
        void cannotActivateWithFewerThanThreeKithkin() {
            addRangerReady(player1);
            addKithkin(player1, 2);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Already-tapped Kithkin cannot be used to pay the cost")
        void tappedKithkinCannotBeUsed() {
            addRangerReady(player1);
            addKithkin(player1, 3);

            gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                    .findFirst().orElseThrow()
                    .tap();

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Non-Kithkin creatures cannot be tapped to pay the cost")
        void nonKithkinCannotBeUsed() {
            addRangerReady(player1);
            addKithkin(player1, 2);

            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bears);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Activating ability does not tap Cloudgoat Ranger")
        void activatingDoesNotTapRanger() {
            Permanent ranger = addRangerReady(player1);
            addKithkin(player1, 3);

            harness.activateAbility(player1, 0, null, null);

            assertThat(ranger.isTapped()).isFalse();
        }
    }

    // ===== Helpers =====

    private void castAndResolveRanger() {
        harness.setHand(player1, List.of(new CloudgoatRanger()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addRangerReady(Player player) {
        Permanent ranger = new Permanent(new CloudgoatRanger());
        ranger.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ranger);
        return ranger;
    }

    private void addKithkin(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent kithkin = new Permanent(createKithkinCard("Test Kithkin " + i));
            kithkin.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(kithkin);
        }
    }

    private Card createKithkinCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.KITHKIN));
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private int countKithkinSoldierTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .count();
    }

    private Permanent findKithkinSoldierToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Kithkin Soldier token found"));
    }

    private void tapKithkin(Player player, int count) {
        List<Permanent> untappedKithkin = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .filter(p -> !p.isTapped())
                .limit(count)
                .toList();
        for (Permanent kithkin : untappedKithkin) {
            harness.handlePermanentChosen(player, kithkin.getId());
        }
    }
}
