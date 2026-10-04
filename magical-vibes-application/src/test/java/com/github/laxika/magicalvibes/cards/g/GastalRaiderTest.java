package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GastalRaider.class, Peek.class, Forest.class, GrizzlyBears.class, CounselOfTheSoratami.class})
class GastalRaiderTest extends BaseCardTest {

    @Test
    void etbRevealsOpponentHandAndDiscardsChosenInstantOrSorcery() {
        Card instant = new Peek();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, List.of(instant, land, creature));
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

    @Test
    void etbCanChooseSorceryAndDiscardsOnlyOneEligibleCard() {
        harness.setHand(player2, List.of(new Peek(), new CounselOfTheSoratami()));
        castRaider(player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Counsel of the Soratami");
        harness.assertInHand(player2, "Peek");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void etbWithNoEligibleCardsRevealsHandWithoutDiscarding() {
        harness.setHand(player2, List.of(new Forest(), new GastalRaider()));
        castRaider(player2.getId());

        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Gastal Raider");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("reveals their hand")).isTrue();
    }

    @Test
    void etbWithEmptyHandStillStartsEngines() {
        harness.setHand(player2, List.of());
        castRaider(player2.getId());

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsMaxSpeedDoesNotGrantBonusAndSpeedThreeIsNotMaxSpeed() {
        Permanent raider = addCreatureReady(player1, new GastalRaider());
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.MENACE)).isFalse();
    }

    private void castRaider(UUID targetId) {
        harness.setHand(player1, List.of(new GastalRaider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();
    }
}
