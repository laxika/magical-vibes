package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrowOfDarkTidingsTest extends BaseCardTest {

    @Test
    void millsTwoCardsWhenItEnters() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new LightningBolt();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new CrowOfDarkTidings()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void millsTwoCardsWhenItDies() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new LightningBolt();
        harness.setLibrary(player1, List.of(first, second, third));
        Permanent crow = harness.addToBattlefieldAndReturn(player1, new CrowOfDarkTidings());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, crow.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first, second, crow.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(crow.getCard().getId()));
    }
}
