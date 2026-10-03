package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.l.LumenClassFrigate;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SunstarChaplain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeyondTheQuiet.class, GrizzlyBears.class, MindStone.class,
        LumenClassFrigate.class, SunstarChaplain.class, Plains.class})
class BeyondTheQuietTest extends BaseCardTest {

    @Test
    void exilesAllCreaturesAndSpacecraftButLeavesOtherPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new LumenClassFrigate());
        harness.addToBattlefield(player2, new MindStone());

        harness.setHand(player1, List.of(new BeyondTheQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Lumen-Class Frigate");
        harness.assertOnBattlefield(player2, "Mind Stone");
    }

    @Test
    void exilesRealCreaturesAndUnstationedSpacecraftForBothPlayers() {
        SunstarChaplain firstCreature = new SunstarChaplain();
        SunstarChaplain secondCreature = new SunstarChaplain();
        LumenClassFrigate firstSpacecraft = new LumenClassFrigate();
        LumenClassFrigate secondSpacecraft = new LumenClassFrigate();
        harness.addToBattlefield(player1, firstCreature);
        harness.addToBattlefield(player2, secondCreature);
        harness.addToBattlefield(player1, firstSpacecraft);
        harness.addToBattlefield(player2, secondSpacecraft);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new BeyondTheQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        for (Card card : List.of(firstCreature, secondCreature, firstSpacecraft, secondSpacecraft)) {
            assertThat(gd.findExiledCard(card.getId())).isNotNull();
        }
        harness.assertNotOnBattlefield(player1, "Sunstar Chaplain");
        harness.assertNotOnBattlefield(player2, "Sunstar Chaplain");
        harness.assertNotOnBattlefield(player1, "Lumen-Class Frigate");
        harness.assertNotOnBattlefield(player2, "Lumen-Class Frigate");
        harness.assertNotInGraveyard(player1, "Sunstar Chaplain");
        harness.assertNotInGraveyard(player2, "Sunstar Chaplain");
        harness.assertNotInGraveyard(player1, "Lumen-Class Frigate");
        harness.assertNotInGraveyard(player2, "Lumen-Class Frigate");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player1, "Beyond the Quiet");
    }

    @Test
    void resolvesWhenThereAreNoCreaturesOrSpacecraft() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new BeyondTheQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player1, "Beyond the Quiet");
        assertThat(gd.stack).isEmpty();
    }

}
