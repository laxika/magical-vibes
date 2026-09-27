package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.Eradicate;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PatronOfTheAkki;
import com.github.laxika.magicalvibes.cards.r.RibbonsOfTheReikai;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorrentOfStone.class, RibbonsOfTheReikai.class, Eradicate.class,
        PatronOfTheAkki.class, Mountain.class})
class TorrentOfStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsFourDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatronOfTheAkki());
        harness.setHand(player1, List.of(new TorrentOfStone()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Torrent of Stone");
    }

    @Test
    @DisplayName("Splices by sacrificing two Mountains and leaves the card in hand")
    void splicesBySacrificingTwoMountains() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatronOfTheAkki());
        RibbonsOfTheReikai arcaneHost = new RibbonsOfTheReikai();
        TorrentOfStone torrent = new TorrentOfStone();
        harness.setHand(player1, List.of(arcaneHost, torrent));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1), List.of(mountain1.getId(), mountain2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .doesNotContain("Mountain");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(torrent);
    }

    @Test
    @DisplayName("Requires two Mountains to pay the splice cost")
    void requiresTwoMountainsToSplice() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatronOfTheAkki());
        RibbonsOfTheReikai arcaneHost = new RibbonsOfTheReikai();
        harness.setHand(player1, List.of(arcaneHost, new TorrentOfStone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1), List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice 2 permanents");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mountain);
    }

    @Test
    @DisplayName("Requires Mountains rather than arbitrary permanents for the splice cost")
    void requiresMountainsForSpliceCost() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent nonMountain = harness.addToBattlefieldAndReturn(player1, new PatronOfTheAkki());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatronOfTheAkki());
        RibbonsOfTheReikai arcaneHost = new RibbonsOfTheReikai();
        harness.setHand(player1, List.of(arcaneHost, new TorrentOfStone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1),
                List.of(mountain.getId(), nonMountain.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mountain, nonMountain);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatronOfTheAkki());
        harness.setHand(player1, List.of(new Eradicate(), new TorrentOfStone()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1),
                List.of(mountain1.getId(), mountain2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced onto Eradicate");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mountain1, mountain2);
    }
}
