package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nihiloor.class, GrizzlyBears.class})
class NihiloorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps your creature and steals an opponent creature within its power")
    void entersTapsAndSteals() {
        Permanent tapper = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castNihiloor();

        PendingInteraction.MultiPermanentChoice tapChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(tapChoice).isNotNull();
        assertThat(tapChoice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(tapper.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(tapper.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    @DisplayName("Attacking with an opponent-owned creature drains its owner")
    void attackingOpponentOwnedCreatureDrainsItsOwner() {
        addCreatureReady(player1, new Nihiloor());
        Permanent stolenAttacker = addCreatureReady(player1, new GrizzlyBears());
        gd.stolenCreatures.put(stolenAttacker.getId(), player2.getId());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void castNihiloor() {
        harness.setHand(player1, List.of(new Nihiloor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
