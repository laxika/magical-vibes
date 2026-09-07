package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NantukoBlightcutter.class, BlackKnight.class, DoomBlade.class, Shock.class})
class NantukoBlightcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from black")
    void hasProtectionFromBlack() {
        harness.addToBattlefield(player1, new NantukoBlightcutter());
        Permanent blightcutter = findBlightcutter();

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blightcutter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Gets +1/+1 for each black permanent opponents control at threshold")
    void boostsForOpponentsBlackPermanentsAtThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new NantukoBlightcutter());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player2, new BlackKnight());
        harness.addToBattlefield(player2, new BlackKnight());

        GameData gd = harness.getGameData();
        Permanent blightcutter = findBlightcutter();

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost below threshold")
    void doesNotBoostBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addToBattlefield(player1, new NantukoBlightcutter());
        harness.addToBattlefield(player2, new BlackKnight());
        harness.addToBattlefield(player2, new BlackKnight());

        GameData gd = harness.getGameData();
        Permanent blightcutter = findBlightcutter();

        assertThat(gqs.getEffectivePower(gd, blightcutter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blightcutter)).isEqualTo(2);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }

    private Permanent findBlightcutter() {
        return findPermanent(player1, "Nantuko Blightcutter");
    }
}
