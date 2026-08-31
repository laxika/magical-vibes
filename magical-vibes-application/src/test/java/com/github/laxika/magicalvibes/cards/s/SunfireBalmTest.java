package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunfireBalm.class, Shock.class, GrizzlyBears.class})
class SunfireBalmTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next four damage to the target player")
    void preventsNextFourDamageToPlayer() {
        harness.setHand(player1, List.of(new SunfireBalm(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 3);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Cycling and accepting the optional prevention draws a card")
    void cyclingAcceptsPreventionAndDraws() {
        harness.setHand(player1, List.of(new SunfireBalm(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInGraveyard(player1, "Sunfire Balm");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the cycling prevention still draws a card")
    void cyclingDeclinesPreventionAndDraws() {
        harness.setHand(player1, List.of(new SunfireBalm(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Sunfire Balm");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
