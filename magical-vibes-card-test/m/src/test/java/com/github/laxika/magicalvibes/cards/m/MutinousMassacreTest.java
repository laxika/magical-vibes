package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutinousMassacre.class, GrizzlyBears.class, LlanowarElves.class,
        Ornithopter.class, DarksteelColossus.class})
class MutinousMassacreTest extends BaseCardTest {

    @Test
    @DisplayName("Resolution waits for the odd/even choice")
    void waitsForParityChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("ODD", "EVEN");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Choosing odd destroys odd creatures, then steals, untaps, and hastes the rest")
    void oddDestroysOddAndTakesRemainingCreatures() {
        Permanent ownOdd = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        ownOdd.tap();
        Permanent opponentEven = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentEven.tap();
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleListChoice(player1, "ODD");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownOdd);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card ->
                card.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentEven);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentEven);
        assertThat(opponentEven.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentEven, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Choosing even destroys even creatures and the temporary control gain expires")
    void evenDestroysEvenAndExpiresAtEndOfTurn() {
        Permanent opponentOdd = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent ownEven = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownEven.tap();
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleListChoice(player1, "EVEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownEven);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentOdd);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentOdd);
        assertThat(gqs.hasKeyword(gd, opponentOdd, Keyword.HASTE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentOdd);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentOdd);
        assertThat(gqs.hasKeyword(gd, opponentOdd, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Zero mana value is even on both battlefields")
    void evenDestroysZeroManaValueCreatures() {
        Permanent ownZero = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opponentZero = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleListChoice(player1, "EVEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownZero, opponentZero);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentZero);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownZero.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentZero.getCard());
    }

    @Test
    @DisplayName("Indestructible creatures of the chosen parity survive and are taken, untapped, and hasted")
    void indestructibleCreatureOfChosenParityIsStillTaken() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new DarksteelColossus());
        survivor.tap();
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleListChoice(player1, "ODD");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(survivor);
        assertThat(survivor.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Existing own survivors are untapped and hasted, but later creatures are unaffected")
    void ownSurvivorsBenefitButLaterCreaturesDoNot() {
        Permanent ownSurvivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownSurvivor.tap();
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleListChoice(player1, "ODD");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownSurvivor);
        assertThat(ownSurvivor.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, ownSurvivor, Keyword.HASTE)).isTrue();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HASTE)).isFalse();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new MutinousMassacre()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
