package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KamiOfThePaintedRoadTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell lets Kami of the Painted Road gain protection from a chosen color")
    void arcaneSpellGrantsChosenProtection() {
        Permanent kami = addCreatureReady(player1, new KamiOfThePaintedRoad());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

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
}
