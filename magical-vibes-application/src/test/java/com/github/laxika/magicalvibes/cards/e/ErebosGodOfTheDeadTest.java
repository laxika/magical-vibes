package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErebosGodOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Erebos is not a creature below five devotion to black")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent erebos = addErebos();
        addBlackPermanents(3);

        assertThat(gqs.isCreature(gd, erebos)).isFalse();
        assertThat(gqs.isEnchantment(gd, erebos)).isTrue();
    }

    @Test
    @DisplayName("Erebos becomes a creature at five devotion to black")
    void becomesCreatureAtDevotionThreshold() {
        Permanent erebos = addErebos();
        addBlackPermanents(4);

        assertThat(gqs.isCreature(gd, erebos)).isTrue();
    }

    @Test
    @DisplayName("Erebos prevents opponents from gaining life but not its controller")
    void preventsOpponentsFromGainingLife() {
        addErebos();

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Paying {1}{B} and 2 life draws a card")
    void payingManaAndLifeDrawsACard() {
        harness.addToBattlefield(player1, new ErebosGodOfTheDead());
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private Permanent addErebos() {
        return harness.addToBattlefieldAndReturn(player1, new ErebosGodOfTheDead());
    }

    private void addBlackPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new WalkingCorpse());
        }
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
