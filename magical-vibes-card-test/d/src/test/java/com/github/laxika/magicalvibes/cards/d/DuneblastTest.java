package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AbzanBanner;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KinTreeWarden;
import com.github.laxika.magicalvibes.cards.s.SaguMauler;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Duneblast.class, GrizzlyBears.class, LlanowarElves.class, AbzanBanner.class,
        KinTreeWarden.class, SaguMauler.class, WetlandSambar.class})
class DuneblastTest extends BaseCardTest {

    @Test
    void controllerCanKeepAnOpponentsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        castDuneblast();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.DestroyRestChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(harness.getPermanentId(player2, "Llanowar Elves")));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    void choosingNoCreatureDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        castDuneblast();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void withNoCreaturesThereIsNoChoice() {
        castDuneblast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Duneblast");
    }

    @Test
    void controllerCanKeepTheirOwnCreature() {
        harness.addToBattlefield(player1, new WetlandSambar());
        harness.addToBattlefield(player2, new KinTreeWarden());
        castDuneblast();

        harness.handleMultiplePermanentsChosen(player1, List.of(harness.getPermanentId(player1, "Wetland Sambar")));

        harness.assertOnBattlefield(player1, "Wetland Sambar");
        harness.assertNotInGraveyard(player1, "Wetland Sambar");
        harness.assertNotOnBattlefield(player2, "Kin-Tree Warden");
        harness.assertInGraveyard(player2, "Kin-Tree Warden");
    }

    @Test
    void opponentsHexproofCreatureCanBeChosenToSurvive() {
        harness.addToBattlefield(player1, new WetlandSambar());
        harness.addToBattlefield(player2, new SaguMauler());
        castDuneblast();

        harness.handleMultiplePermanentsChosen(player1, List.of(harness.getPermanentId(player2, "Sagu Mauler")));

        harness.assertOnBattlefield(player2, "Sagu Mauler");
        harness.assertInGraveyard(player1, "Wetland Sambar");
        harness.assertNotOnBattlefield(player1, "Wetland Sambar");
    }

    @Test
    void choosingNoneDestroysHexproofCreaturesButLeavesNoncreatures() {
        harness.addToBattlefield(player1, new AbzanBanner());
        harness.addToBattlefield(player2, new SaguMauler());
        castDuneblast();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Abzan Banner");
        harness.assertNotInGraveyard(player1, "Abzan Banner");
        harness.assertNotOnBattlefield(player2, "Sagu Mauler");
        harness.assertInGraveyard(player2, "Sagu Mauler");
    }

    @Test
    void unchosenCreatureCanRegenerate() {
        var warden = harness.addToBattlefieldAndReturn(player1, new KinTreeWarden());
        harness.addToBattlefield(player2, new WetlandSambar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        castDuneblast();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Kin-Tree Warden");
        harness.assertNotInGraveyard(player1, "Kin-Tree Warden");
        assertThat(warden.isTapped()).isTrue();
        assertThat(warden.getRegenerationShield()).isZero();
        harness.assertNotOnBattlefield(player2, "Wetland Sambar");
        harness.assertInGraveyard(player2, "Wetland Sambar");
    }

    private void castDuneblast() {
        harness.castFromHand(player1, new Duneblast(), "{4}{W}{B}{G}");
        harness.passBothPriorities();
    }
}
