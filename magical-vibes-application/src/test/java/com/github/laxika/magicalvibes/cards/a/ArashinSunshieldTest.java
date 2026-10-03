package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArashinSunshield.class, GrizzlyBears.class, LightningBolt.class})
class ArashinSunshieldTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to two cards from a single graveyard when it enters")
    void exilesCardsFromSingleGraveyardOnEnter() {
        Card first = new GrizzlyBears();
        Card second = new LightningBolt();
        Card untouched = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second, untouched));
        harness.setHand(player1, List.of(new ArashinSunshield()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Pays white and taps a target creature")
    void tapsTargetCreature() {
        Permanent sunshield = harness.addToBattlefieldAndReturn(player1, new ArashinSunshield());
        sunshield.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(sunshield.isTapped()).isTrue();
    }

    @Test
    void mayChooseNoCardsFromNonemptyGraveyard() {
        Card card = new ArashinSunshield();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new ArashinSunshield()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void mayExileOneCardFromOwnGraveyard() {
        Card chosen = new ArashinSunshield();
        Card untouched = new ArashinSunshield();
        harness.setGraveyard(player1, List.of(chosen, untouched));
        harness.setHand(player1, List.of(new ArashinSunshield()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
    }

    @Test
    void cannotChooseCardsFromDifferentGraveyards() {
        Card own = new ArashinSunshield();
        Card opposing = new ArashinSunshield();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opposing));
        harness.setHand(player1, List.of(new ArashinSunshield()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(own.getId(), opposing.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(opposing.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(own);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opposing);
    }

    @Test
    void exilesRemainingTargetWhenOtherTargetLeavesGraveyard() {
        Card removed = new ArashinSunshield();
        Card remaining = new ArashinSunshield();
        harness.setGraveyard(player2, List.of(removed, remaining));
        harness.setHand(player1, List.of(new ArashinSunshield()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removed);
    }

    @Test
    void cannotPayTapCostWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArashinSunshield());
        source.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArashinSunshield());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }
}
