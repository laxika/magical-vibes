package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IchneumonDruid.class, Shock.class, GrizzlyBears.class})
class IchneumonDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage for the second and each later instant an opponent casts")
    void damagesOpponentForSecondAndLaterInstants() {
        harness.addToBattlefield(player1, new IchneumonDruid());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 8);
    }

    @Test
    @DisplayName("Counts instant spells separately from other spells")
    void nonInstantSpellDoesNotCountAsFirstInstant() {
        harness.addToBattlefield(player1, new IchneumonDruid());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Does not trigger for instants cast by its controller")
    void doesNotTriggerForController() {
        harness.addToBattlefield(player1, new IchneumonDruid());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
