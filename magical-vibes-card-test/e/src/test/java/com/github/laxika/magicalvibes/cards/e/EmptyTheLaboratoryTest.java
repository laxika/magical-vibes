package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArcaneSignet;
import com.github.laxika.magicalvibes.cards.c.CemeteryReaper;
import com.github.laxika.magicalvibes.cards.u.UndeadAugur;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WarpathGhoul;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmptyTheLaboratory.class, WarpathGhoul.class, Gravecrawler.class,
        Island.class, GrizzlyBears.class, ArcaneSignet.class, CemeteryReaper.class, UndeadAugur.class})
class EmptyTheLaboratoryTest extends BaseCardTest {

    @Test
    void sacrificesXZombiesAndPutsTheMatchingRevealedCardsOntoTheBattlefield() {
        Card battlefieldZombie = new WarpathGhoul();
        Card battlefieldCrawler = new Gravecrawler();
        harness.addToBattlefield(player1, battlefieldZombie);
        harness.addToBattlefield(player1, battlefieldCrawler);

        Card island = new Island();
        Card revealedZombie = new WarpathGhoul();
        Card bears = new GrizzlyBears();
        Card revealedCrawler = new Gravecrawler();
        harness.setLibrary(player1, List.of(island, revealedZombie, bears, revealedCrawler));
        harness.setHand(player1, List.of(new EmptyTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(battlefieldZombie, battlefieldCrawler);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(revealedZombie, revealedCrawler)
                .doesNotContain(battlefieldZombie, battlefieldCrawler);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(island, bears);
    }

    @Test
    void withZeroXItDoesNotSacrificeOrReveal() {
        Card battlefieldZombie = new WarpathGhoul();
        harness.addToBattlefield(player1, battlefieldZombie);
        Card island = new Island();
        Card libraryZombie = new WarpathGhoul();
        harness.setLibrary(player1, List.of(island, libraryZombie));
        harness.setHand(player1, List.of(new EmptyTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactly(battlefieldZombie);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(island, libraryZombie);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(battlefieldZombie);
    }

    @Test
    void revealsOnlyForZombiesActuallySacrificedWhenXIsTooLarge() {
        Card sacrificed = new CemeteryReaper();
        Card opponentsZombie = new CemeteryReaper();
        harness.addToBattlefield(player1, sacrificed);
        harness.addToBattlefield(player2, opponentsZombie);
        Card skipped = new ArcaneSignet();
        Card revealed = new CemeteryReaper();
        Card untouched = new CemeteryReaper();
        harness.setLibrary(player1, List.of(skipped, revealed, untouched));
        harness.setHand(player1, List.of(new EmptyTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(revealed);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard).containsExactly(opponentsZombie);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, skipped);
    }

    @Test
    void putsAllFoundZombiesOntoBattlefieldWhenLibraryRunsOut() {
        harness.addToBattlefield(player1, new CemeteryReaper());
        harness.addToBattlefield(player1, new CemeteryReaper());
        Card firstMiss = new ArcaneSignet();
        Card found = new CemeteryReaper();
        Card lastMiss = new ArcaneSignet();
        harness.setLibrary(player1, List.of(firstMiss, found, lastMiss));
        harness.setHand(player1, List.of(new EmptyTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(found);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstMiss, lastMiss);
    }

    @Test
    void allowsControllerToChooseWhichZombieToSacrificeAndResumesRevealing() {
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new CemeteryReaper());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new CemeteryReaper());
        Card found = new CemeteryReaper();
        Card untouched = new ArcaneSignet();
        harness.setLibrary(player1, List.of(found, untouched));
        harness.setHand(player1, List.of(new EmptyTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleMultiplePermanentsChosen(player1, List.of(sacrificed.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactlyInAnyOrder(kept.getCard(), found);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    void sacrificesZombiesSimultaneouslySoDyingAugurSeesAllDeaths() {
        Card augur = new UndeadAugur();
        Card otherZombie = new CemeteryReaper();
        harness.addToBattlefield(player1, augur);
        harness.addToBattlefield(player1, otherZombie);
        harness.setLibrary(player1, List.of(new ArcaneSignet(), new ArcaneSignet(), new ArcaneSignet()));
        harness.setHand(player1, List.of(new EmptyTheLaboratory()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2);
        for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(augur, otherZombie);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
