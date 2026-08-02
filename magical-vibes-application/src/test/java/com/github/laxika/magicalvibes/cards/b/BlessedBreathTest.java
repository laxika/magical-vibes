package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlessedBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gains protection from the chosen color")
    void grantsProtectionFromChosenColor() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Hill Giant"));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");

        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and leaves Blessed Breath in hand")
    void splicesOntoArcaneSpell() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(arcaneShock, new BlessedBreath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithSplice(player1, 0, harness.getPermanentId(player1, "Hill Giant"), List.of(1));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Blessed Breath");
    }
}
