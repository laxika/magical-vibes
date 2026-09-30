package com.github.laxika.magicalvibes.cards.e;

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
        Island.class, GrizzlyBears.class})
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

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactly(battlefieldZombie);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(island, libraryZombie);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(battlefieldZombie);
    }
}
