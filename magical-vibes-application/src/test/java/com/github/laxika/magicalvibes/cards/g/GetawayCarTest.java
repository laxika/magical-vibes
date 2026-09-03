package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GetawayCar.class, GrizzlyBears.class})
class GetawayCarTest extends BaseCardTest {

    @Test
    void attackingReturnsTheCreatureThatCrewedItThisTurn() {
        addReadyCar(player1);
        Permanent crewer = addCreatureReady(player1, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());

        crewCar(player1, crewer);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(crewer.getId());

        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crewer);
        assertThat(gd.playerHands.get(player1.getId())).contains(crewer.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bystander);
        assertThat(crewer.isTapped()).isTrue();
    }

    @Test
    void blockingReturnsTheCreatureThatCrewedItThisTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReadyCar(player2);
        Permanent crewer = addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        crewCar(player2, crewer);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(crewer.getId());

        harness.handlePermanentChosen(player2, crewer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(crewer);
        assertThat(gd.playerHands.get(player2.getId())).contains(crewer.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bystander);
        assertThat(crewer.isTapped()).isTrue();
    }

    private Permanent addReadyCar(Player player) {
        Permanent car = harness.addToBattlefieldAndReturn(player, new GetawayCar());
        car.setSummoningSick(false);
        return car;
    }

    private void crewCar(Player player, Permanent crewer) {
        harness.activateAbility(player, 0, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player, crewer.getId());
        }
        harness.passBothPriorities();
    }
}
