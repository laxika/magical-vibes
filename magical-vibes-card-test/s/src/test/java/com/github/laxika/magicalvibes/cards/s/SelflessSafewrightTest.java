package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelflessSafewright.class})
class SelflessSafewrightTest extends BaseCardTest {

    private static Card createPermanent(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        if (type != CardType.CREATURE) {
            card.setAdditionalTypes(Set.of(CardType.KINDRED));
        }
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
        harness.castFromHand(player1, new SelflessSafewright(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
    }

    @Test
    @DisplayName("Entering the battlefield prompts for a creature type")
    void enteringPromptsForCreatureType() {
        harness.castFromHand(player1, new SelflessSafewright(), "{3}{G}{G}");
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

    @Test
    void canBeCastDuringCombatWithFlash() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        castAndChooseElf();

        assertThat(findPermanent(player1, "Selfless Safewright")).isNotNull();
    }

    @Test
    void convokeCanPayTheEntireCostWithSummoningSickCreatures() {
        List<Permanent> elves = IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new SelflessSafewright()))
                .toList();
        elves.forEach(elf -> elf.setSummoningSick(true));
        harness.setHand(player1, List.of(new SelflessSafewright()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                elves.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(6);
        assertThat(elves).allSatisfy(elf -> {
            assertThat(elf.isTapped()).isTrue();
            assertThat(gqs.hasKeyword(gd, elf, Keyword.HEXPROOF)).isTrue();
            assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isTrue();
        });
    }

    @Test
    void laterEnteringPermanentsDoNotReceiveTheGrant() {
        castAndChooseElf();

        Permanent laterElf = harness.addToBattlefieldAndReturn(player1, new SelflessSafewright());

        assertThat(gqs.hasKeyword(gd, laterElf, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterElf, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void canChooseATypeWithNoMatchingPermanents() {
        harness.castFromHand(player1, new SelflessSafewright(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleListChoice(player1, "HUMAN");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Selfless Safewright"),
                Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
