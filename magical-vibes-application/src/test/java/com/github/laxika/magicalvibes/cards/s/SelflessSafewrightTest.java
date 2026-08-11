package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SelflessSafewrightTest extends BaseCardTest {

    private static Card createPermanent(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setSubtypes(List.of(subtypes));
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }
        return card;
    }

    private void castAndChooseElf() {
        harness.setHand(player1, List.of(new SelflessSafewright()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
    }

    @Test
    @DisplayName("Entering the battlefield prompts for a creature type")
    void enteringPromptsForCreatureType() {
        harness.setHand(player1, List.of(new SelflessSafewright()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Other matching permanents you control gain hexproof and indestructible")
    void grantsKeywordsToOtherMatchingOwnPermanents() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1,
                createPermanent("Elf Relic", CardType.ARTIFACT, CardSubtype.ELF));
        Permanent human = harness.addToBattlefieldAndReturn(player1,
                createPermanent("Human", CardType.CREATURE, CardSubtype.HUMAN));
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2,
                createPermanent("Opponent Elf", CardType.CREATURE, CardSubtype.ELF));

        castAndChooseElf();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Selfless Safewright"), Keyword.HEXPROOF))
                .isFalse();
    }

    @Test
    @DisplayName("The keyword grants expire during cleanup")
    void grantsExpireAtEndOfTurn() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1,
                createPermanent("Elf", CardType.CREATURE, CardSubtype.ELF));

        castAndChooseElf();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
