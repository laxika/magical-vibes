package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MephidrossSlime.class})
class MephidrossSlimeTest extends BaseCardTest {

    @Test
    @DisplayName("On death, it conjures a perpetually doubled copy and shuffles both cards into their owners' libraries")
    void conjuresDoubledCopyAndShufflesBothCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent slime = harness.addToBattlefieldAndReturn(player1, new MephidrossSlime());
        harness.setLibrary(player1, List.of());

        slime.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> slimes = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Mephidross Slime"))
                .toList();
        assertThat(slimes).hasSize(2);
        assertThat(slimes).extracting(Card::getId).doesNotHaveDuplicates();
        assertThat(slimes).allSatisfy(card -> assertThat(gd.perpetualCardPowerToughnessModifiers)
                .containsEntry(card.getId(), new CardPowerToughnessModifier(3, 3)));
    }

    @Test
    @DisplayName("The trigger does not resolve after the dying card leaves the graveyard")
    void triggerDoesNothingIfDyingCardIsGone() {
        Permanent slime = harness.addToBattlefieldAndReturn(player1, new MephidrossSlime());
        harness.setLibrary(player1, List.of());
        slime.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
