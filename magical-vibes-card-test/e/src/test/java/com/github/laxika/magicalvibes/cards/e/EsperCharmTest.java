package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicBenediction;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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

@CardUsed({EsperCharm.class, AngelicBenediction.class, CylianElf.class})
class EsperCharmTest extends BaseCardTest {

    private void addWUB() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Nested
    @DisplayName("Mode 0: Destroy target enchantment")
    @CardUsed({EsperCharm.class, AngelicBenediction.class, CylianElf.class})
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            Permanent anthem = harness.addToBattlefieldAndReturn(player2, new AngelicBenediction());
            harness.setHand(player1, List.of(new EsperCharm()));
            addWUB();

            harness.castInstant(player1, 0, 0, anthem.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Angelic Benediction");
            harness.assertInGraveyard(player2, "Angelic Benediction");
        }

        @Test
        @DisplayName("Cannot target a creature with the enchantment mode")
        void cannotTargetCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new CylianElf());
            // A valid enchantment target must exist so the spell is castable at all.
            harness.addToBattlefield(player1, new AngelicBenediction());
            harness.setHand(player1, List.of(new EsperCharm()));
            addWUB();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Draw two cards")
    @CardUsed({EsperCharm.class, AngelicBenediction.class, CylianElf.class})
    class DrawTwoMode {

        @Test
        @DisplayName("Controller draws two cards")
        void drawsTwo() {
            harness.setHand(player1, new ArrayList<>(List.of(new EsperCharm())));
            harness.setLibrary(player1, List.of(new CylianElf(), new CylianElf(), new CylianElf()));
            addWUB();

            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target player discards two cards")
    @CardUsed({EsperCharm.class, AngelicBenediction.class, CylianElf.class})
    class DiscardTwoMode {

        @Test
        @DisplayName("Target player discards two chosen cards")
        void targetDiscardsTwo() {
            harness.setHand(player2, new ArrayList<>(List.of(new AngelicBenediction(), new CylianElf(), new AngelicBenediction())));
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
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new AngelicBenediction());
        harness.setHand(player1, List.of(new EsperCharm()));
        addWUB();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 99, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDestroyOwnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AngelicBenediction());
        harness.setHand(player1, List.of(new EsperCharm()));
        addWUB();

        harness.castInstant(player1, 0, 0, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angelic Benediction");
        harness.assertInGraveyard(player1, "Angelic Benediction");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canTargetSelfForDiscard() {
        harness.setHand(player1, List.of(new EsperCharm(), new CylianElf(),
                new AngelicBenediction(), new CylianElf()));
        addWUB();

        harness.castInstant(player1, 0, 2, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Cylian Elf");
        harness.assertInGraveyard(player1, "Angelic Benediction");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void discardsOnlyAvailableCardFromShortHand() {
        harness.setHand(player2, List.of(new CylianElf()));
        harness.setHand(player1, List.of(new EsperCharm()));
        addWUB();

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Cylian Elf");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Esper Charm");
    }

    @Test
    void canTargetPlayerWithEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new EsperCharm()));
        addWUB();

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Esper Charm");
    }
}
