package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimelyHordemateTest extends BaseCardTest {

    private void castTimelyHordemate() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TimelyHordemate()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Raid returns a chosen creature with mana value 2 or less")
    void raidReturnsCheapCreature() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        GrizzlyBears cheapCreature = new GrizzlyBears();
        HillGiant expensiveCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(expensiveCreature, cheapCreature));

        castTimelyHordemate();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(cheapCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof HillGiant)
                .noneMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Raid does not trigger if no creature attacked this turn")
    void noRaidDoesNotTrigger() {
        GrizzlyBears cheapCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(cheapCreature));

        castTimelyHordemate();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cheapCreature);
    }

    @Test
    @DisplayName("Raid ignores creature cards with mana value greater than 2")
    void ignoresExpensiveCreature() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        HillGiant expensiveCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(expensiveCreature));

        castTimelyHordemate();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expensiveCreature);
    }
}
