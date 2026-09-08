package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GastalRaiderTest extends BaseCardTest {

    @Test
    void etbRevealsOpponentHandAndDiscardsChosenInstantOrSorcery() {
        Card instant = new Peek();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(instant, land, creature)));
        castRaider(player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Grizzly Bears");
    }

    @Test
    void maxSpeedGrantsPlusOnePlusOneAndMenace() {
        Permanent raider = addCreatureReady(player1, new GastalRaider());
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.MENACE)).isFalse();

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.MENACE)).isTrue();
    }

    @Test
    void etbCannotTargetAPlayerControlledPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GastalRaider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This spell can only target players");
    }

    private void castRaider(UUID targetId) {
        harness.setHand(player1, List.of(new GastalRaider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
