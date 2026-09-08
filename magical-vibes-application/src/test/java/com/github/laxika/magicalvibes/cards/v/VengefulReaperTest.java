package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VengefulReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying, deathtouch, and haste on the battlefield")
    void hasPrintedKeywords() {
        harness.addToBattlefield(player1, new VengefulReaper());

        Permanent reaper = findPermanent(player1, "Vengeful Reaper");

        assertThat(gqs.hasKeyword(gd, reaper, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, reaper, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, reaper, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        VengefulReaper reaper = new VengefulReaper();
        harness.setHand(player1, List.of(reaper));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(reaper.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, reaper.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vengeful Reaper");
    }
}
