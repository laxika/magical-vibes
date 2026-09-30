package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GravePeril;
import com.github.laxika.magicalvibes.cards.l.LlanowarEmpath;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CentaurOmenreader.class, LlanowarEmpath.class, GravePeril.class})
class CentaurOmenreaderTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Centaur Omenreader reduces creature spell costs by {2}")
    void tappedSourceReducesCreatureSpellCost() {
        harness.addToBattlefield(player1, new CentaurOmenreader());
        Permanent omenreader = findPermanent(player1, "Centaur Omenreader");
        omenreader.tap();
        harness.castFromHand(player1, new LlanowarEmpath(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Untapped Centaur Omenreader does not reduce creature spell costs")
    void untappedSourceDoesNotReduceCreatureSpellCost() {
        harness.addToBattlefield(player1, new CentaurOmenreader());

        assertThatThrownBy(() -> harness.castFromHand(player1, new LlanowarEmpath(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped Centaur Omenreader does not reduce noncreature spell costs")
    void tappedSourceDoesNotReduceNoncreatureSpellCost() {
        harness.addToBattlefield(player1, new CentaurOmenreader());
        findPermanent(player1, "Centaur Omenreader").tap();

        assertThatThrownBy(() -> harness.castFromHand(player1, new GravePeril(), "{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped Centaur Omenreader does not reduce an opponent's creature spell")
    void tappedSourceDoesNotReduceOpponentsCreatureSpell() {
        harness.addToBattlefield(player1, new CentaurOmenreader());
        findPermanent(player1, "Centaur Omenreader").tap();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromHand(player2, new LlanowarEmpath(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }
}
