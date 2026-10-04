package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.VoiceOfResurgence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattleForBretagard.class, VoiceOfResurgence.class})
class BattleForBretagardTest extends BaseCardTest {

    @Test
    @DisplayName("Casting the Saga triggers chapter I as it enters")
    void enteringTriggersChapterI() {
        harness.castFromHand(player1, new BattleForBretagard(), "{1}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countOf("Human Warrior")).isEqualTo(1);
        assertThat(countOf("Elf Warrior")).isZero();
        assertThat(saga().getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapters I and II create Human Warrior and Elf Warrior tokens")
    void chaptersCreateWarriorTokens() {
        harness.addToBattlefield(player1, new BattleForBretagard());
        Permanent saga = saga();
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        assertThat(countOf("Human Warrior")).isEqualTo(1);

        advanceToNextChapter();
        assertThat(countOf("Elf Warrior")).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III copies a chosen distinct-name set of artifact and creature tokens")
    void chapterIIICopiesChosenDistinctTokens() {
        harness.addToBattlefield(player1, new BattleForBretagard());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, token("Bear", CardType.CREATURE, 2, 2));
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, token("Bear", CardType.CREATURE, 2, 2));
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, token("Treasure", CardType.ARTIFACT, 0, 0));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, token("Elf", CardType.CREATURE, 1, 1));
        harness.addToBattlefield(player1, token("Land Token", CardType.LAND, 0, 0));

        Permanent saga = saga();
        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different names");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstBear.getId(), treasure.getId(), elf.getId()));

        assertThat(countOf("Bear")).isEqualTo(3);
        assertThat(countOf("Treasure")).isEqualTo(2);
        assertThat(countOf("Elf")).isEqualTo(2);
        assertThat(countOf("Land Token")).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III allows choosing no tokens and then sacrifices the Saga")
    void chapterIIICanChooseNoTokens() {
        harness.addToBattlefield(player1, new BattleForBretagard());
        harness.addToBattlefield(player1, token("Bear Token", CardType.CREATURE, 2, 2));
        saga().setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(countOf("Bear Token")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Battle for Bretagard");
        harness.assertInGraveyard(player1, "Battle for Bretagard");
    }

    @Test
    @DisplayName("Chapter III ignores opposing tokens and nontoken creatures")
    void chapterIIIWithNoEligibleTokens() {
        harness.addToBattlefield(player1, new BattleForBretagard());
        harness.addToBattlefield(player2, token("Bear Token", CardType.CREATURE, 2, 2));
        Card nontoken = token("Bear Token", CardType.CREATURE, 2, 2);
        nontoken.setToken(false);
        harness.addToBattlefield(player1, nontoken);
        harness.addToBattlefield(player1, token("Land Token", CardType.LAND, 0, 0));
        saga().setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf("Bear Token")).isEqualTo(1);
        assertThat(countOf("Land Token")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Battle for Bretagard");
    }

    @Test
    @DisplayName("Chapter III preserves every color of a multicolored token copy")
    void chapterIIIPreservesMultipleColors() {
        harness.addToBattlefield(player1, new BattleForBretagard());
        VoiceOfResurgence tokenCopy = new VoiceOfResurgence();
        tokenCopy.setToken(true);
        Permanent original = harness.addToBattlefieldAndReturn(player1, tokenCopy);
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        saga().setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of(original.getId()));

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Voice of Resurgence"))
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(harness.getGameQueryService().getEffectiveColors(gd, copy))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent saga() {
        return harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Battle for Bretagard"));
    }

    private long countOf(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .count();
    }

    private static Card token(String name, CardType type, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("");
        card.setToken(true);
        card.setColor(type == CardType.CREATURE ? CardColor.GREEN : null);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
