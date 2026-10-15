package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.cards.r.RipjawRaptor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({DeathlessAncient.class, QueensBaySoldier.class, RipjawRaptor.class})
class DeathlessAncientTest extends BaseCardTest {


    @Nested
    @DisplayName("Graveyard activated ability")
    @CardUsed({DeathlessAncient.class, QueensBaySoldier.class, RipjawRaptor.class})
    class GraveyardAbilityTests {

        @Test
        void summoningSickVampiresCanPayCost() {
            DeathlessAncient ancient = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient));
            for (int i = 0; i < 3; i++) {
                harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier()).setSummoningSick(true);
            }

            harness.activateGraveyardAbility(player1, 0);

            assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
            harness.passBothPriorities();
            assertThat(gd.playerHands.get(player1.getId())).contains(ancient);
        }

        @Test
        void opponentsVampiresCannotPayCost() {
            harness.setGraveyard(player1, List.of(new DeathlessAncient()));
            addVampires(player1, 2);
            addVampires(player2, 3);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(Permanent::isTapped);
        }

        @Test
        void returnsOnlyTheActivatedCopy() {
            DeathlessAncient ancient = new DeathlessAncient();
            DeathlessAncient other = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient, other));
            addVampires(player1, 3);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).contains(ancient).doesNotContain(other);
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        }

        @Test
        @DisplayName("Activating ability puts it on the stack")
        void activatingPutsOnStack() {
            DeathlessAncient ancient = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient));
            addVampires(player1, 3);

            harness.activateGraveyardAbility(player1, 0);

            // Choose 3 vampires to tap
            tapVampires(player1, 3);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            assertThat(gd.stack.getFirst().getCard()).isSameAs(ancient);
        }

        @Test
        @DisplayName("Resolving ability returns Deathless Ancient from graveyard to hand")
        void resolvingReturnsToHand() {
            DeathlessAncient ancient = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient));
            addVampires(player1, 3);

            harness.activateGraveyardAbility(player1, 0);
            tapVampires(player1, 3);
            harness.passBothPriorities();

            harness.assertInHand(player1, "Deathless Ancient");
            harness.assertNotInGraveyard(player1, "Deathless Ancient");
        }

        @Test
        @DisplayName("Tapping three vampires taps them as cost")
        void tapsVampiresAsCost() {
            DeathlessAncient ancient = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient));
            addVampires(player1, 4); // 4 vampires, only 3 should be tapped

            harness.activateGraveyardAbility(player1, 0);
            tapVampires(player1, 3);

            long tappedCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.VAMPIRE))
                    .filter(Permanent::isTapped)
                    .count();
            assertThat(tappedCount).isEqualTo(3);

            long untappedCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.VAMPIRE))
                    .filter(p -> !p.isTapped())
                    .count();
            assertThat(untappedCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Cannot activate with fewer than 3 vampires")
        void cannotActivateWithFewerThan3Vampires() {
            DeathlessAncient ancient = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient));
            addVampires(player1, 2);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot activate with no vampires on battlefield")
        void cannotActivateWithNoVampires() {
            DeathlessAncient ancient = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient));

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Already-tapped vampires cannot be used to pay the cost")
        void tappedVampiresCannotBeUsed() {
            DeathlessAncient ancient = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient));
            addVampires(player1, 3);

            // Tap one vampire so only 2 untapped remain
            gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.VAMPIRE))
                    .findFirst().orElseThrow()
                    .tap();

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Non-Vampire creatures cannot be tapped to pay the cost")
        void nonVampiresCannotBeUsed() {
            DeathlessAncient ancient = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient));
            addVampires(player1, 2);

            // Add a non-vampire creature
            harness.addToBattlefieldAndReturn(player1, new RipjawRaptor()).setSummoningSick(false);

            // Only 2 vampires + 1 non-vampire = not enough vampires
            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("With exactly 3 vampires, auto-pays the cost without player choice")
        void autoPaysWith3Vampires() {
            DeathlessAncient ancient = new DeathlessAncient();
            harness.setGraveyard(player1, List.of(ancient));
            addVampires(player1, 3);

            harness.activateGraveyardAbility(player1, 0);

            // With exactly 3, it should auto-pay Ă˘â‚¬â€ť ability goes straight to stack
            // (auto-pay path handles the tapping automatically)
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard()).isSameAs(ancient);

            // All 3 vampires should be tapped
            long tappedCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.VAMPIRE))
                    .filter(Permanent::isTapped)
                    .count();
            assertThat(tappedCount).isEqualTo(3);
        }
    }


    private void addVampires(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefieldAndReturn(player, new QueensBaySoldier()).setSummoningSick(false);
        }
    }

    private void tapVampires(Player player, int count) {
        List<Permanent> untappedVampires = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.VAMPIRE))
                .filter(p -> !p.isTapped())
                .limit(count)
                .toList();
        for (Permanent vamp : untappedVampires) {
            harness.handlePermanentChosen(player, vamp.getId());
        }
    }
}
