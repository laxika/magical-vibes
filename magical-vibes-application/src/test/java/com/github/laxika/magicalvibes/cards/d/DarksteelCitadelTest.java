package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EchoingRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelCitadel.class, EchoingRuin.class})
class DarksteelCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds {C}")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Indestructible keeps it on the battlefield through a destroy effect")
    void survivesDestruction() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0, land.getId());

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
    }

    @Test
    @DisplayName("A freshly played Citadel can immediately produce colorless mana")
    void playedLandCanImmediatelyProduceMana() {
        harness.setHand(player1, List.of(new DarksteelCitadel()));

        harness.playLand(player1, 0);
        harness.activateAbility(player1, 0, null, null);

        harness.assertOnBattlefield(player1, "Darksteel Citadel");
        harness.assertNotInHand(player1, "Darksteel Citadel");
        assertThat(findPermanent(player1, "Darksteel Citadel").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Citadel cannot activate its mana ability again")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Echoing Ruin cannot destroy targeted or untargeted Citadels controlled by either player")
    void allCopiesSurviveEchoingRuin() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(countPermanents(player1, "Darksteel Citadel")).isEqualTo(1);
        assertThat(countPermanents(player2, "Darksteel Citadel")).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        harness.assertInGraveyard(player1, "Echoing Ruin");
    }
}
