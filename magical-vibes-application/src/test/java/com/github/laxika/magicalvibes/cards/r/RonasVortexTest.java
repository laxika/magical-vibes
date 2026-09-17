package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RonasVortex.class, GrizzlyBears.class, ChandraNalaar.class, HillGiant.class, Shock.class})
class RonasVortexTest extends BaseCardTest {

    @Test
    void returnsOpponentsCreatureWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell(false);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void putsKickedOpponentsCreatureOnBottomOfOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new HillGiant(), new Shock()));
        prepareSpell(true);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Hill Giant", "Shock", "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void putsKickedOpponentsPlaneswalkerOnBottomOfOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        harness.setLibrary(player2, List.of(new Shock()));
        prepareSpell(true);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Shock", "Chandra Nalaar");
        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
    }

    @Test
    void cannotTargetPermanentControlledByCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSpell(false);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker an opponent controls");
    }

    private void prepareSpell(boolean kicked) {
        harness.setHand(player1, List.of(new RonasVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        if (kicked) {
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
        }
    }
}
