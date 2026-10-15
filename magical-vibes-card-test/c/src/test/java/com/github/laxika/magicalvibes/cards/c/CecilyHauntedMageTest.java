package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JinGitaxiasCoreAugur;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CecilyHauntedMage.class, JinGitaxiasCoreAugur.class, Opt.class, Ponder.class, Forest.class})
class CecilyHauntedMageTest extends BaseCardTest {

    @Test
    @DisplayName("Raises its controller's maximum hand size to eleven")
    void maximumHandSizeIsEleven() {
        harness.addToBattlefield(player1, new CecilyHauntedMage());
        harness.setHand(player1, cards(12));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Draws, loses life, and offers a free hand spell after reaching eleven cards")
    void attacksAndCastsHandSpellForFreeAtElevenCards() {
        Permanent cecily = addAttacker();
        Opt freeSpell = new Opt();
        List<Card> hand = new ArrayList<>(cards(9));
        hand.add(freeSpell);
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cecily)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(11);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(freeSpell.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(freeSpell);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(freeSpell);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(11);
    }

    @Test
    @DisplayName("Still draws and loses life when the post-draw hand has fewer than eleven cards")
    void doesNotOfferSpellBelowElevenCards() {
        Opt freeSpell = new Opt();
        List<Card> hand = new ArrayList<>(cards(8));
        hand.add(freeSpell);
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        addAttacker();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(freeSpell);
    }

    @Test
    @DisplayName("Cecily sets the hand limit to eleven after an earlier reduction")
    void setsHandSizeAfterEarlierReduction() {
        harness.enterBattlefieldAndReturn(player2, new JinGitaxiasCoreAugur());
        harness.enterBattlefieldAndReturn(player1, new CecilyHauntedMage());
        harness.setHand(player1, cards(12));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast a creature even with eleven cards in hand")
    void doesNotOfferCreatureSpell() {
        addAttacker();
        Card creature = new CecilyHauntedMage();
        List<Card> hand = new ArrayList<>(cards(9));
        hand.add(creature);
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(11).contains(creature);
        harness.assertLife(player1, 19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("May decline the free instant while still drawing and losing life")
    void mayDeclineFreeSpell() {
        addAttacker();
        Card spell = new Opt();
        List<Card> hand = new ArrayList<>(cards(9));
        hand.add(spell);
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(11).contains(spell);
        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May cast a newly drawn sorcery with more than eleven cards")
    void castsNewlyDrawnSorceryAboveThreshold() {
        addAttacker();
        Card spell = new Ponder();
        harness.setHand(player1, cards(11));
        harness.setLibrary(player1, List.of(spell));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(12).contains(spell);
        harness.assertLife(player1, 19);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(spell.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(11).doesNotContain(spell);
    }

    private Permanent addAttacker() {
        Permanent cecily = addCreatureReady(player1, new CecilyHauntedMage());
        cecily.setAttackTarget(player2.getId());
        return cecily;
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
