package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeadwaterSentries;
import com.github.laxika.magicalvibes.cards.p.PerilousVoyage;
import com.github.laxika.magicalvibes.cards.p.PiratesCutlass;
import com.github.laxika.magicalvibes.cards.t.ThrashOfRaptors;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakeningSunsAvatar.class, GrizzlyBears.class, BeaconOfUnrest.class,
        ThrashOfRaptors.class, HeadwaterSentries.class, PerilousVoyage.class, PiratesCutlass.class})
class WakeningSunsAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand, all non-Dinosaur creatures are destroyed")
    void castFromHandDestroysNonDinosaurs() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new WakeningSunsAvatar(), "{5}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("When cast from hand, Dinosaur creatures survive")
    void castFromHandSparesDinosaurs() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player2, new ThrashOfRaptors());

        harness.castFromHand(player1, new WakeningSunsAvatar(), "{5}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thrash of Raptors");
        harness.assertOnBattlefield(player2, "Thrash of Raptors");
    }

    @Test
    @DisplayName("Wakening Sun's Avatar itself survives its own ETB since it is a Dinosaur")
    void avatarItselfsurvivestBecauseItIsADinosaur() {
        harness.castFromHand(player1, new WakeningSunsAvatar(), "{5}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wakening Sun's Avatar");
    }

    @Test
    @DisplayName("When entering not from hand, non-Dinosaur creatures are not destroyed")
    void enteringNotFromHandDoesNotDestroyCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        WakeningSunsAvatar target = new WakeningSunsAvatar();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, target.getName());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The destruction trigger still resolves after the Avatar returns to hand")
    void triggerResolvesAfterAvatarLeavesBattlefield() {
        harness.addToBattlefield(player1, new HeadwaterSentries());
        harness.addToBattlefield(player2, new HeadwaterSentries());
        harness.addToBattlefield(player2, new ThrashOfRaptors());
        harness.castFromHand(player1, new WakeningSunsAvatar(), "{5}{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Headwater Sentries");
        harness.setHand(player2, List.of(new PerilousVoyage()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0,
                findPermanent(player1, "Wakening Sun's Avatar").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wakening Sun's Avatar");
        harness.assertInHand(player1, "Wakening Sun's Avatar");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Headwater Sentries");
        harness.assertInGraveyard(player2, "Headwater Sentries");
        harness.assertOnBattlefield(player2, "Thrash of Raptors");
    }

    @Test
    @DisplayName("The destruction trigger spares noncreature permanents on both sides")
    void noncreaturePermanentsSurvive() {
        harness.addToBattlefield(player1, new PiratesCutlass());
        harness.addToBattlefield(player2, new PiratesCutlass());
        harness.addToBattlefield(player2, new HeadwaterSentries());
        harness.castFromHand(player1, new WakeningSunsAvatar(), "{5}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pirate's Cutlass");
        harness.assertOnBattlefield(player2, "Pirate's Cutlass");
        harness.assertInGraveyard(player2, "Headwater Sentries");
        harness.assertOnBattlefield(player1, "Wakening Sun's Avatar");
    }
}
