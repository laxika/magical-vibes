package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NornsDisassembly.class, SolRing.class, MindStone.class, GrizzlyBears.class})
class NornsDisassemblyTest extends BaseCardTest {

    @Test
    void sacrificesHistoricPermanentAndSeeksHistoricCard() {
        harness.addToBattlefield(player1, new NornsDisassembly());
        harness.addToBattlefield(player1, new SolRing());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new MindStone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sol Ring");
        harness.assertInHand(player1, "Mind Stone");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void requiresAHistoricPermanentToSacrifice() {
        harness.addToBattlefield(player1, new NornsDisassembly());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
