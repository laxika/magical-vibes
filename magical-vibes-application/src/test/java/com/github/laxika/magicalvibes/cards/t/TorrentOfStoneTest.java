package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TorrentOfStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsFourDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new TorrentOfStone()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Splices by sacrificing two Mountains and leaves the card in hand")
    void splicesBySacrificingTwoMountains() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card arcaneHost = new HolyDay().createRuntimeCopy();
        arcaneHost.setSubtypes(List.of(CardSubtype.ARCANE));
        TorrentOfStone torrent = new TorrentOfStone();
        harness.setHand(player1, List.of(arcaneHost, torrent));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1), List.of(mountain1.getId(), mountain2.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .doesNotContain("Mountain");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(torrent);
    }

    @Test
    @DisplayName("Requires two Mountains to pay the splice cost")
    void requiresTwoMountainsToSplice() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card arcaneHost = new HolyDay().createRuntimeCopy();
        arcaneHost.setSubtypes(List.of(CardSubtype.ARCANE));
        harness.setHand(player1, List.of(arcaneHost, new TorrentOfStone()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1), List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice 2 permanents");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mountain);
    }
}
