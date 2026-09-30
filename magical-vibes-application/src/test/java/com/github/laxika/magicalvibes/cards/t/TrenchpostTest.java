package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cloudpost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Trenchpost.class, Cloudpost.class})
class TrenchpostTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent trenchpost = harness.addToBattlefieldAndReturn(player1, new Trenchpost());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(trenchpost.isTapped()).isTrue();
    }

    @Test
    void millsOneCardForEachLocusControlled() {
        harness.addToBattlefield(player1, new Trenchpost());
        harness.addToBattlefield(player1, new Cloudpost());
        harness.addToBattlefield(player1, new Cloudpost());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> library = gd.playerDecks.get(player2.getId());
        while (library.size() > 5) {
            library.removeFirst();
        }
        int librarySizeBefore = library.size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }
}
