package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.d.DepartTheRealm;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CycloneSummoner.class, BeaconOfUnrest.class, BlindSpotGiant.class, FugitiveWizard.class,
        GrizzlyBears.class, GloriousAnthem.class, Island.class, Mistwalker.class, GoldveinPick.class,
        DepartTheRealm.class})
class CycloneSummonerTest extends BaseCardTest {

    @Test
    void castFromHandReturnsAllNonExemptPermanents() {
        harness.addToBattlefield(player1, new BlindSpotGiant());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new CycloneSummoner(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cyclone Summoner");
        harness.assertOnBattlefield(player1, "Blind-Spot Giant");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void enteringWithoutBeingCastFromHandDoesNotReturnPermanents() {
        harness.addToBattlefield(player1, new BlindSpotGiant());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new Island());
        CycloneSummoner target = new CycloneSummoner();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cyclone Summoner");
        harness.assertOnBattlefield(player1, "Blind-Spot Giant");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    void changelingsRemainWhileArtifactsReturnToHand() {
        harness.addToBattlefield(player1, new Mistwalker());
        harness.addToBattlefield(player2, new Mistwalker());
        harness.addToBattlefield(player1, new GoldveinPick());
        harness.addToBattlefield(player2, new GoldveinPick());
        harness.addToBattlefield(player2, new Island());

        harness.castFromHand(player1, new CycloneSummoner(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistwalker");
        harness.assertOnBattlefield(player2, "Mistwalker");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertOnBattlefield(player1, "Cyclone Summoner");
        harness.assertInHand(player1, "Goldvein Pick");
        harness.assertInHand(player2, "Goldvein Pick");
        harness.assertNotOnBattlefield(player1, "Goldvein Pick");
        harness.assertNotOnBattlefield(player2, "Goldvein Pick");
    }

    @Test
    void borrowedPermanentReturnsToItsOwnerInsteadOfItsController() {
        GoldveinPick borrowed = new GoldveinPick();
        borrowed.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, borrowed);

        harness.castFromHand(player1, new CycloneSummoner(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goldvein Pick");
        harness.assertInHand(player2, "Goldvein Pick");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void triggerStillReturnsPermanentsAfterSummonerLeavesBattlefield() {
        harness.addToBattlefield(player2, new GoldveinPick());
        harness.castFromHand(player1, new CycloneSummoner(), "{5}{U}{U}");
        harness.passBothPriorities();

        var summonerId = harness.getPermanentId(player1, "Cyclone Summoner");
        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, summonerId);
        harness.assertInHand(player1, "Cyclone Summoner");
        harness.assertOnBattlefield(player2, "Goldvein Pick");

        harness.passBothPriorities();

        harness.assertInHand(player2, "Goldvein Pick");
        harness.assertNotOnBattlefield(player2, "Goldvein Pick");
    }
}
