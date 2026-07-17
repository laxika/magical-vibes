package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EsperCharmTest extends BaseCardTest {

    private void addWUB() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Nested
    @DisplayName("Mode 0: Destroy target enchantment")
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            harness.addToBattlefield(player2, new GloriousAnthem());
            harness.setHand(player1, List.of(new EsperCharm()));
            addWUB();

            Permanent anthem = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Glorious Anthem"))
                    .findFirst().orElseThrow();

            harness.castInstant(player1, 0, 0, anthem.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Glorious Anthem"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Glorious Anthem"));
        }

        @Test
        @DisplayName("Cannot target a creature with the enchantment mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            // A valid enchantment target must exist so the spell is castable at all.
            harness.addToBattlefield(player1, new GloriousAnthem());
            harness.setHand(player1, List.of(new EsperCharm()));
            addWUB();

            Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Draw two cards")
    class DrawTwoMode {

        @Test
        @DisplayName("Controller draws two cards")
        void drawsTwo() {
            harness.setHand(player1, new ArrayList<>(List.of(new EsperCharm())));
            harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
            addWUB();

            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target player discards two cards")
    class DiscardTwoMode {

        @Test
        @DisplayName("Target player discards two chosen cards")
        void targetDiscardsTwo() {
            harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new GrizzlyBears(), new Peek())));
            harness.setHand(player1, List.of(new EsperCharm()));
            addWUB();

            harness.castInstant(player1, 0, 2, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player2, 0);
            harness.handleCardChosen(player2, 0);

            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        }
    }

    @Test
    @DisplayName("Choosing an invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new EsperCharm()));
        addWUB();

        Permanent anthem = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glorious Anthem"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 99, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
