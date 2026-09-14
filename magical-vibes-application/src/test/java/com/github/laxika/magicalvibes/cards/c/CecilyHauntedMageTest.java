package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CecilyHauntedMage.class, Forest.class, GrizzlyBears.class})
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
        GrizzlyBears freeSpell = new GrizzlyBears();
        List<com.github.laxika.magicalvibes.model.Card> hand = new ArrayList<>(cards(9));
        hand.add(freeSpell);
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cecily)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(11);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(freeSpell.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(freeSpell);
    }

    @Test
    @DisplayName("Still draws and loses life when the post-draw hand has fewer than eleven cards")
    void doesNotOfferSpellBelowElevenCards() {
        GrizzlyBears freeSpell = new GrizzlyBears();
        List<com.github.laxika.magicalvibes.model.Card> hand = new ArrayList<>(cards(8));
        hand.add(freeSpell);
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        addAttacker();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(freeSpell);
    }

    private Permanent addAttacker() {
        Permanent cecily = addCreatureReady(player1, new CecilyHauntedMage());
        cecily.setAttackTarget(player2.getId());
        return cecily;
    }

    private List<com.github.laxika.magicalvibes.model.Card> cards(int count) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
