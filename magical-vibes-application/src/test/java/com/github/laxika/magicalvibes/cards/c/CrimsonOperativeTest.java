package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrimsonOperative.class, GrizzlyBears.class, Shock.class})
class CrimsonOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top card with play permission through the owner's next turn")
    void etbExilesTopCardUntilNextTurn() {
        Card top = new Shock();
        Card below = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, below));

        castCrimsonOperative();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(top.getId()))
                .isEqualTo(gd.turnNumber + (player1.getId().equals(gd.activePlayerId) ? 2 : 1));
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(top.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("ETB does nothing when the library is empty")
    void etbDoesNothingWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castCrimsonOperative();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Prowess boosts Crimson Operative for a noncreature spell")
    void prowessBoostsForNoncreatureSpell() {
        Permanent operative = addReadyOperative();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess does not trigger for a creature spell")
    void prowessDoesNotTriggerForCreatureSpell() {
        Permanent operative = addReadyOperative();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
    }

    private void castCrimsonOperative() {
        harness.setHand(player1, List.of(new CrimsonOperative()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyOperative() {
        Permanent operative = addCreatureReady(player1, new CrimsonOperative());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return operative;
    }
}
