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
    @DisplayName("The trigger still conjures and shuffles a doubled card after the original is exiled")
    void triggerStillConjuresIfDyingCardIsGone() {
        Permanent slime = harness.addToBattlefieldAndReturn(player1, new MephidrossSlime());
        harness.setLibrary(player1, List.of());
        slime.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(slime.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerDecks.get(player1.getId()).getFirst();
        assertThat(duplicate.getId()).isNotEqualTo(slime.getCard().getId());
        assertThat(gd.perpetualCardPowerToughnessModifiers)
                .containsEntry(duplicate.getId(), new CardPowerToughnessModifier(3, 3));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A second death doubles the original again but conjures a fresh doubled card")
    void repeatedDeathDoublesOriginalAgain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent slime = harness.addToBattlefieldAndReturn(player1, new MephidrossSlime());
        harness.setLibrary(player1, List.of());
        slime.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Card original = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(slime.getCard().getId()))
                .findFirst().orElseThrow();
        gd.playerDecks.get(player1.getId()).remove(original);
        harness.castFromHand(player1, original, "{1}{B}{G}");
        harness.passBothPriorities();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(6);
        returned.setMarkedDamage(6);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.perpetualCardPowerToughnessModifiers)
                .containsEntry(original.getId(), new CardPowerToughnessModifier(9, 9));
        assertThat(gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> !card.getId().equals(original.getId())))
                .allSatisfy(card -> assertThat(gd.perpetualCardPowerToughnessModifiers)
                        .containsEntry(card.getId(), new CardPowerToughnessModifier(3, 3)));
    }

    @Test
    @DisplayName("A stolen Slime returns to its owner while the conjured card belongs to its controller")
    void stolenSlimeAndDuplicateGoToDifferentLibraries() {
        MephidrossSlime card = new MephidrossSlime();
        card.setOwnerId(player2.getId());
        Permanent slime = harness.addToBattlefieldAndReturn(player1, card);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        slime.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerDecks.get(player1.getId()).getFirst();
        assertThat(duplicate.getOwnerId()).isEqualTo(player1.getId());
        assertThat(duplicate.getId()).isNotEqualTo(card.getId());
        assertThat(gd.perpetualCardPowerToughnessModifiers)
                .containsEntry(card.getId(), new CardPowerToughnessModifier(3, 3))
                .containsEntry(duplicate.getId(), new CardPowerToughnessModifier(3, 3));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
