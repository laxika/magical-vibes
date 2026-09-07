package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HeartlessSummoning;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CentaurOmenreader.class, HillGiant.class, HeartlessSummoning.class})
class CentaurOmenreaderTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Centaur Omenreader reduces creature spell costs by {2}")
    void tappedSourceReducesCreatureSpellCost() {
        harness.addToBattlefield(player1, new CentaurOmenreader());
        Permanent omenreader = findPermanent(player1, "Centaur Omenreader");
        omenreader.tap();
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Untapped Centaur Omenreader does not reduce creature spell costs")
    void untappedSourceDoesNotReduceCreatureSpellCost() {
        harness.addToBattlefield(player1, new CentaurOmenreader());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped Centaur Omenreader does not reduce noncreature spell costs")
    void tappedSourceDoesNotReduceNoncreatureSpellCost() {
        harness.addToBattlefield(player1, new CentaurOmenreader());
        findPermanent(player1, "Centaur Omenreader").tap();
        harness.setHand(player1, List.of(new HeartlessSummoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
