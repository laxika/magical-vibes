package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RakshasasDisdainTest extends BaseCardTest {

    private void castRakshasasDisdainOnBears(GrizzlyBears bears) {
        harness.setHand(player1, List.of(bears));
        harness.setHand(player2, List.of(new RakshasasDisdain()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
    }

    @Test
    @DisplayName("Counters a spell when its controller cannot pay for the graveyard cards")
    void countersWhenControllerCannotPay() {
        harness.setGraveyard(player2, List.of(new GiantGrowth(), new GrizzlyBears()));

        GrizzlyBears bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast, only 1 left over

        castRakshasasDisdainOnBears(bears);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lets the spell resolve when its controller pays for the graveyard cards")
    void payingGraveyardCardCountLetsSpellResolve() {
        harness.setGraveyard(player2, List.of(new GiantGrowth(), new GrizzlyBears()));

        GrizzlyBears bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.GREEN, 4); // 2 to cast, 2 to pay

        castRakshasasDisdainOnBears(bears);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Has no payment requirement when its controller has no cards in their graveyard")
    void noGraveyardCardsRequiresNoMana() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.GREEN, 2);

        castRakshasasDisdainOnBears(bears);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
