package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LumengridAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws, discards an artifact, and untaps Lumengrid Augur")
    void artifactDiscardUntapsAugur() {
        Permanent augur = addReadyAugur(player1);
        harness.setHand(player2, List.of(new FountainOfYouth()));
        setDeck(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        int artifactIndex = findCardIndexByType(gd.playerHands.get(player2.getId()), CardType.ARTIFACT);
        harness.handleCardChosen(player2, artifactIndex);

        assertThat(augur.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Discarding a nonartifact card does not untap Lumengrid Augur")
    void nonartifactDiscardDoesNotUntapAugur() {
        Permanent augur = addReadyAugur(player1);
        harness.setHand(player2, List.of(new Cancel()));
        setDeck(player2, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        int nonartifactIndex = findCardIndexByType(gd.playerHands.get(player2.getId()), CardType.INSTANT);
        harness.handleCardChosen(player2, nonartifactIndex);

        assertThat(augur.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Cancel");
    }

    private Permanent addReadyAugur(Player player) {
        Permanent perm = new Permanent(new LumengridAugur());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    private int findCardIndexByType(List<Card> cards, CardType type) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).hasType(type)) {
                return i;
            }
        }
        return -1;
    }
}
