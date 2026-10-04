package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IlluminatedFolio.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, Ornithopter.class, SafeholdElite.class})
class IlluminatedFolioTest extends BaseCardTest {

    @Test
    @DisplayName("Activating with two color-sharing cards puts the ability on the stack and taps the Folio")
    void activatingWithSharingPairTapsAndStacks() {
        Permanent folio = addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Two green cards share a color.
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(folio.isTapped()).isTrue();
        // Revealed cards stay in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals") && log.contains("as a cost"));
    }

    @Test
    @DisplayName("Resolving the ability draws a card")
    void resolvingDrawsACard() {
        addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(3);
        assertThat(hand.get(2).getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Cannot activate without two cards that share a color")
    void cannotActivateWithoutSharingPair() {
        addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Green + red + colorless: no two share a color.
        harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant(), new Ornithopter()));
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a color");
    }

    @Test
    @DisplayName("Two colorless cards cannot pay the reveal cost")
    void cannotRevealTwoColorlessCards() {
        Permanent folio = addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new IlluminatedFolio(), new IlluminatedFolio()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a color");
        assertThat(folio.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A single multicolored card cannot count as two cards")
    void cannotRevealOneCardTwice() {
        addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new SafeholdElite()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a color");
    }

    @Test
    @DisplayName("A hybrid card shares green with a monocolored green card")
    void hybridCardCanPayRevealCostWithGreenCard() {
        addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        Card hybrid = new SafeholdElite();
        Card green = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(hybrid, green));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hybrid, green, drawn);
    }

    @Test
    @DisplayName("The controller chooses which pair to reveal when multiple pairs qualify")
    void doesNotAutomaticallyRevealFirstPairWhenThereIsAChoice() {
        Permanent folio = addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new SafeholdElite()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(folio.isTapped()).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals") && log.contains("as a cost"));
    }

    private Permanent addReadyFolio(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new IlluminatedFolio());
        perm.setSummoningSick(false);
        return perm;
    }
}
