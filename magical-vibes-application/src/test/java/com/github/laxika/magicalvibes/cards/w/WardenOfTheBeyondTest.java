package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WardenOfTheBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("Gets no bonus when no opponent owns a card in exile")
    void noBonusWithoutOpponentOwnedExiledCard() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Gets +2/+2 when an opponent owns a card in exile")
    void getsBonusForOpponentOwnedExiledCard() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        harness.setExile(player2, List.of(new Spellbook()));

        assertStats(4, 4);
    }

    @Test
    @DisplayName("Does not count a card the controller owns in exile")
    void doesNotCountControllersExiledCard() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        harness.setExile(player1, List.of(new Spellbook()));

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Loses the bonus when the opponent-owned exiled card leaves exile")
    void losesBonusWhenExiledCardLeaves() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        Card exiledCard = new Spellbook();
        harness.setExile(player2, List.of(exiledCard));
        assertStats(4, 4);

        gd.removeFromExile(exiledCard.getId());

        assertStats(2, 2);
    }

    private void assertStats(int power, int toughness) {
        Permanent warden = findPermanent(player1, "Warden of the Beyond");
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(toughness);
    }
}
