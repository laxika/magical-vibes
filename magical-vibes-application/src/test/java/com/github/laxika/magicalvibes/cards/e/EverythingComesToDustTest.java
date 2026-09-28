package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({EverythingComesToDust.class, ElvishWarrior.class, WirewoodElf.class,
        GlorySeeker.class, DarksteelRelic.class, GloriousAnthem.class})
class EverythingComesToDustTest extends BaseCardTest {

    @Test
    void sparesCreaturesSharingATypeWithAConvokingCreatureAndExilesOtherPermanentTypes() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new WirewoodElf());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new EverythingComesToDust()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convoker.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Warrior");
        harness.assertOnBattlefield(player2, "Wirewood Elf");
        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertNotOnBattlefield(player1, "Darksteel Relic");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void withoutConvokeExilesAllCreatures() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new WirewoodElf());
        harness.setHand(player1, List.of(new EverythingComesToDust()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elvish Warrior");
        harness.assertNotOnBattlefield(player2, "Wirewood Elf");
    }
}
