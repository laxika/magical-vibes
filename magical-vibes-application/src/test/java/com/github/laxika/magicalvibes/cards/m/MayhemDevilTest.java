package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MayhemDevil.class, KuldothaRebirth.class, Spellbook.class})
class MayhemDevilTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to any target when its controller sacrifices a permanent")
    void triggersForControllerSacrifice() {
        harness.addToBattlefield(player1, new MayhemDevil());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        castKuldothaRebirth(player1, artifact);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).contains(player2.getId());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Triggers when an opponent sacrifices a noncreature permanent")
    void triggersForOpponentSacrifice() {
        harness.addToBattlefield(player1, new MayhemDevil());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        castKuldothaRebirth(player2, artifact);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).contains(player1.getId());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInGraveyard(player2, "Spellbook");
    }

    private void castKuldothaRebirth(com.github.laxika.magicalvibes.model.Player player,
                                     Permanent artifact) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new KuldothaRebirth()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player, 0, artifact.getId());
    }
}
