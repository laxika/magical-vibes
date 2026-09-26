package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamiOfThePaintedRoad.class, DesperateRitual.class, HarshDeceiver.class,
        DevotedRetainer.class, YamabushisFlame.class})
class KamiOfThePaintedRoadTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell lets Kami of the Painted Road gain protection from a chosen color")
    void arcaneSpellGrantsChosenProtection() {
        Permanent kami = addCreatureReady(player1, new KamiOfThePaintedRoad());
        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(kami.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Casting a Spirit spell lets Kami of the Painted Road gain protection from a chosen color")
    void spiritSpellGrantsChosenProtection() {
        Permanent kami = addCreatureReady(player1, new KamiOfThePaintedRoad());
        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(kami.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger Kami of the Painted Road")
    void unrelatedSpellDoesNotTrigger() {
        Permanent kami = addCreatureReady(player1, new KamiOfThePaintedRoad());
        harness.setHand(player1, List.of(new DevotedRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(kami.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Kami of the Painted Road does not trigger for an opponent's Spirit spell")
    void opponentSpiritSpellDoesNotTrigger() {
        Permanent kami = addCreatureReady(player1, new KamiOfThePaintedRoad());
        harness.setHand(player2, List.of(new HarshDeceiver()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(kami.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Chosen protection prevents a spell of that color from targeting Kami of the Painted Road")
    void chosenProtectionStopsMatchingSpellFromTargetingKami() {
        Permanent kami = addCreatureReady(player1, new KamiOfThePaintedRoad());
        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        harness.setHand(player2, List.of(new YamabushisFlame()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, kami.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection granted by Kami of the Painted Road wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent kami = addCreatureReady(player1, new KamiOfThePaintedRoad());
        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        resolveAllTriggers();

        assertThat(gqs.hasProtectionFrom(gd, kami, CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, kami, CardColor.BLUE)).isFalse();
    }
}
