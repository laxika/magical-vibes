package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Necrosavant")
class NecrosavantTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Activating during upkeep sacrifices a creature and resolves it back to the battlefield")
    void activatingReturnsToBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setGraveyard(player1, List.of(new Necrosavant()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        addMana(player1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        // Grizzly Bears was sacrificed to pay the cost
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Necrosavant is on the battlefield untapped and no longer in the graveyard
        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Necrosavant"))
                .findFirst().orElseThrow();
        assertThat(perm.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Necrosavant"));
    }

    @Test
    @DisplayName("Cannot activate outside of your upkeep")
    void cannotActivateOutsideUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Necrosavant()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        addMana(player1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setGraveyard(player1, List.of(new Necrosavant()));
        addMana(player1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
