package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoleculeMan.class, GrizzlyBears.class, Forest.class})
class MoleculeManTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a drawn nonland card for miracle {0}")
    void castsDrawnNonlandCardForZeroMana() {
        harness.addToBattlefield(player1, new MoleculeMan());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        drawAndProcessMiracleReveal();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.manaCost()).isEqualTo("{0}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not grant miracle to a land card")
    void doesNotGrantMiracleToLand() {
        harness.addToBattlefield(player1, new MoleculeMan());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        drawCard();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    private void drawAndProcessMiracleReveal() {
        drawCard();
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
