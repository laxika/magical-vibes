package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.o.Overture;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JidoorAristocraticCapital.class, Overture.class, Forest.class})
class JidoorAristocraticCapitalTest extends BaseCardTest {

    @Test
    @DisplayName("Jidoor enters tapped and produces blue mana")
    void entersTappedAndProducesBlueMana() {
        harness.setHand(player1, List.of(new JidoorAristocraticCapital()));

        harness.playLand(player1, 0);
        Permanent jidoor = findPermanent(player1, "Jidoor, Aristocratic Capital");
        assertThat(jidoor.isTapped()).isTrue();

        jidoor.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adventure mills half of an opponent's library rounded down and exiles Jidoor")
    void adventureMillsOpponentAndExilesLand() {
        JidoorAristocraticCapital jidoor = new JidoorAristocraticCapital();
        harness.setHand(player1, List.of(jidoor));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCardWithAdventure(gd, player1, 0, 0, player2.getId(), null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(jidoor.getId()));

        harness.castFromExile(player1, jidoor.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(jidoor.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(jidoor.getId()));
    }

    @Test
    @DisplayName("Adventure cannot target its controller")
    void adventureCannotTargetController() {
        JidoorAristocraticCapital jidoor = new JidoorAristocraticCapital();
        harness.setHand(player1, List.of(jidoor));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCardWithAdventure(
                gd, player1, 0, 0, player1.getId(), null, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(jidoor);
    }
}
