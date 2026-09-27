package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BlastedLandscape;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolcanicOffering.class, BlastedLandscape.class, GrizzlyBears.class})
class VolcanicOfferingTest extends BaseCardTest {

    @Test
    void controllerAndOpponentChooseTheTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BlastedLandscape());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VolcanicOffering()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(land.getId(), creature.getId()));

        PendingInteraction.PermanentChoice opponentForLand =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentForLand.playerId()).isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, land.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, creature.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof BlastedLandscape)
                .anyMatch(card -> card instanceof GrizzlyBears);
    }
}
