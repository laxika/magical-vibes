package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZurgoAndOjutai.class, ShivanDragon.class, GrizzlyBears.class})
class ZurgoAndOjutaiTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof during the turn it enters")
    void hasHexproofDuringEnteringTurn() {
        harness.setHand(player1, List.of(new ZurgoAndOjutai()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent zurgo = findPermanent(player1, "Zurgo and Ojutai");
        assertThat(gqs.hasKeyword(gd, zurgo, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, zurgo, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("A Dragon combat-damage trigger looks at three cards and may return a Dragon")
    void looksAtCardsAndMayReturnDragon() {
        addCreatureReady(player1, new ZurgoAndOjutai());
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        Permanent nonDragon = addCreatureReady(player1, new GrizzlyBears());
        dragon.setAttacking(true);
        nonDragon.setAttacking(true);

        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandTopBottomChoice.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandTopBottom(0, 1));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, dragon.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonDragon).doesNotContain(dragon);
    }

    @Test
    @DisplayName("Unchosen cards go beneath the untouched library cards")
    void unchosenCardsGoToBottom() {
        Permanent dragon = addCreatureReady(player1, new ZurgoAndOjutai());
        dragon.setAttacking(true);
        Card first = new ZurgoAndOjutai();
        Card second = new ZurgoAndOjutai();
        Card third = new ZurgoAndOjutai();
        Card untouched = new ZurgoAndOjutai();
        harness.setLibrary(player1, List.of(first, second, third, untouched));
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.HandTopBottom(0, 1));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, second, third);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
    }

    @Test
    @DisplayName("A single library card goes to hand and the damaging source may return")
    void singleCardLibraryStillAllowsReturningSource() {
        Permanent dragon = addCreatureReady(player1, new ZurgoAndOjutai());
        dragon.setAttacking(true);
        Card onlyCard = new ZurgoAndOjutai();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard, dragon.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dragon);
    }

    @Test
    @DisplayName("An empty library does not prevent returning the damaging Dragon")
    void emptyLibraryStillAllowsReturningSource() {
        Permanent dragon = addCreatureReady(player1, new ZurgoAndOjutai());
        dragon.setAttacking(true);
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dragon.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dragon);
    }

    @Test
    @DisplayName("Multiple Dragons damaging the same player create only one trigger")
    void multipleDragonsCreateOneTrigger() {
        Permanent zurgo = addCreatureReady(player1, new ZurgoAndOjutai());
        Permanent otherDragon = addCreatureReady(player1, new ShivanDragon());
        zurgo.setAttacking(true);
        otherDragon.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
    }
}
