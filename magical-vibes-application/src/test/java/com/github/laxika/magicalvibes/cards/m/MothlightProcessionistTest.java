package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MothlightProcessionist.class, DazzlingTheaterPropRoom.class})
class MothlightProcessionistTest extends BaseCardTest {

    @Test
    void enchantmentEntryConjuresAndDiscardsTheExactCardAtNextEndStep() {
        harness.addToBattlefieldAndReturn(player1, new MothlightProcessionist());
        harness.setHand(player1, List.of(testEnchantment()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card unrelated = testCard("Unrelated card", CardType.INSTANT);
        gd.addCardToHand(player1.getId(), unrelated);
        Card conjured = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Mothlight Processionist"))
                .findFirst()
                .orElseThrow();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(conjured);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(unrelated);
    }

    @Test
    void fullyUnlockingARoomConjuresACard() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.addToBattlefieldAndReturn(player1, new MothlightProcessionist());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mothlight Processionist"));
    }

    @Test
    void enchantmentSpellsCanUseConvoke() {
        harness.addToBattlefieldAndReturn(player1, new MothlightProcessionist());
        Card convokerCard = testCard("Ready convoker", CardType.CREATURE);
        convokerCard.setPower(1);
        convokerCard.setToughness(1);
        Permanent convoker = addCreatureReady(player1, convokerCard);
        Card enchantment = testEnchantment();
        harness.setHand(player1, List.of(enchantment));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();
        harness.passBothPriorities();
    }

    private Card testEnchantment() {
        return testCard("Test enchantment", CardType.ENCHANTMENT);
    }

    private Card testCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        return card;
    }
}
