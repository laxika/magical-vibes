package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LabyrinthChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Labyrinth Champion makes it deal 2 damage to any target")
    void castingSpellThatTargetsChampionDealsDamageToAnyTarget() {
        harness.addToBattlefield(player1, new LabyrinthChampion());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Labyrinth Champion"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Labyrinth Champion")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new LabyrinthChampion());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
