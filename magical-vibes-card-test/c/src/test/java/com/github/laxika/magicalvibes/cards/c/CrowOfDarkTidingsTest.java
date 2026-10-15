package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrowOfDarkTidings.class, Forest.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class CrowOfDarkTidingsTest extends BaseCardTest {

    @Test
    void millsTwoCardsWhenItEnters() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new LightningBolt();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new CrowOfDarkTidings(), "{2}{B}");
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

        harness.castAndResolveInstant(player1, 0, crow.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first, second, crow.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(crow.getCard().getId()));
    }

    @Test
    void millsTheOnlyRemainingCardWhenItEnters() {
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(remaining));

        harness.castFromHand(player1, new CrowOfDarkTidings(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entryTriggerResolvesWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new CrowOfDarkTidings(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Crow of Dark Tidings");
    }

    @Test
    void deathTriggerMillsItsControllerRatherThanTheKillingSpellsController() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card opponentsCard = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(opponentsCard));
        Permanent crow = harness.addToBattlefieldAndReturn(player2, new CrowOfDarkTidings());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, crow.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(crow.getCard(), first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsCard);
        harness.assertNotOnBattlefield(player2, "Crow of Dark Tidings");
    }
}
