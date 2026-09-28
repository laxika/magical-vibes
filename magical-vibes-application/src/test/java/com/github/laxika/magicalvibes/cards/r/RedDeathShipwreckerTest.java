package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedDeathShipwrecker.class, GrizzlyBears.class})
class RedDeathShipwreckerTest extends BaseCardTest {

    @Test
    @DisplayName("Goads an opponent's creature, makes its controller draw, and adds red mana")
    void goadsCreatureDrawsForItsControllerAndAddsRedMana() {
        Permanent source = addReadySource(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        addReadySource(player1);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySource(Player player) {
        return addCreatureReady(player, new RedDeathShipwrecker());
    }
}
