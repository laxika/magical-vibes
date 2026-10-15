package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InstantRamen.class, Forest.class})
class InstantRamenTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void enteringTheBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new InstantRamen()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrificing it gains 3 life")
    void sacrificingItGainsThreeLife() {
        harness.addToBattlefield(player1, new InstantRamen());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        harness.assertInGraveyard(player1, "Instant Ramen");
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's end step")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new InstantRamen()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Instant Ramen");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the draw trigger survives its source")
    void canSacrificeBeforeDrawTriggerResolves() {
        harness.setHand(player1, List.of(new InstantRamen()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Forest");

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Instant Ramen");
        harness.assertInGraveyard(player1, "Instant Ramen");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertNotInHand(player1, "Forest");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A tapped Ramen cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new InstantRamen());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Instant Ramen");
        harness.assertNotInGraveyard(player1, "Instant Ramen");
    }

    @Test
    @DisplayName("One mana is insufficient to activate the Food ability")
    void cannotActivateWithoutTwoMana() {
        harness.addToBattlefield(player1, new InstantRamen());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Instant Ramen");
        harness.assertNotInGraveyard(player1, "Instant Ramen");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }
}
