package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LazavDimirMastermindTest extends BaseCardTest {

    private Permanent lazav() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard() instanceof LazavDimirMastermind)
                .findFirst()
                .orElseThrow();
    }

    private void millCreature(com.github.laxika.magicalvibes.model.Card creature) {
        harness.setLibrary(player2, List.of(creature, new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Tome Scour resolves, mills the creature card
        harness.passBothPriorities(); // Lazav's trigger resolves, queueing the may prompt
    }

    @Test
    @DisplayName("Becomes a copy of a creature card milled into an opponent's graveyard, keeping its name, legendary, and hexproof")
    void becomesCopyOfMilledCreature() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        var card = lazav().getCard();
        assertThat(card.getName()).isEqualTo("Lazav, Dimir Mastermind");
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(card.getKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Declining the may choice leaves Lazav unchanged")
    void decliningLeavesLazavUnchanged() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, false);

        var card = lazav().getCard();
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Keeps this ability after copying, so it can copy again")
    void keepsAbilityAfterCopying() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        millCreature(new Ornithopter());
        harness.handleMayAbilityChosen(player1, true);

        var card = lazav().getCard();
        assertThat(card.getName()).isEqualTo("Lazav, Dimir Mastermind");
        assertThat(card.getPower()).isZero();
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getKeywords()).contains(Keyword.HEXPROOF, Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not trigger when a creature card is put into the controller's own graveyard")
    void doesNotTriggerForOwnGraveyard() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities(); // Tome Scour resolves, milling the controller's own creature

        assertThat(gd.stack).isEmpty();
        var card = lazav().getCard();
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
    }
}
