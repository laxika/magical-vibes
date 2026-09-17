package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonstrousWarLeech.class, Forest.class, Shock.class, Divination.class,
        GrizzlyBears.class, ColossalDreadmaw.class})
class MonstrousWarLeechTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the greatest mana value in your graveyard")
    void powerAndToughnessUseGreatestOwnGraveyardManaValue() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new ColossalDreadmaw()));

        Permanent leech = new Permanent(new MonstrousWarLeech());
        leech.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(leech);

        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(3);
    }

    @Test
    @DisplayName("Unkicked entry does not mill")
    void unkickedEntryDoesNotMill() {
        harness.setHand(player1, List.of(new MonstrousWarLeech()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Kicked entry mills four cards")
    void kickedEntryMillsFourCards() {
        harness.setHand(player1, List.of(new MonstrousWarLeech()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Forest"))
                .hasSize(4);
    }
}
