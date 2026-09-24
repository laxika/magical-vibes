package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarosGoneNuts.class, AnointedProcession.class, BladeSplicer.class,
        FurnaceOfRath.class, LightningBolt.class})
class MarosGoneNutsTest extends BaseCardTest {

    @Test
    @DisplayName("Maro's Gone Nuts quadruples an effect that doubles token creation")
    void quadruplesAnEffectThatDoublesTokens() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(4);
    }

    @Test
    @DisplayName("Maro's Gone Nuts does not alter an effect that does not double")
    void doesNotAlterNonDoublingEffects() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
    }

    @Test
    @DisplayName("Maro's Gone Nuts makes Furnace of Rath quadruple damage")
    void quadruplesGlobalDamageDoubler() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }
}
