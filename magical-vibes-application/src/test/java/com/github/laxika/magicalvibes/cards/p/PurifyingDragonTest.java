package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StitchedDrake;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PurifyingDragon.class, GrizzlyBears.class, StitchedDrake.class})
class PurifyingDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals 1 damage to a non-Zombie creature defending player controls")
    void attacksDealOneDamageToNonZombie() {
        addCreatureReady(player1, new PurifyingDragon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking deals 2 damage to a Zombie defending player controls")
    void attacksDealTwoDamageToZombie() {
        addCreatureReady(player1, new PurifyingDragon());
        Permanent target = addCreatureReady(player2, new StitchedDrake());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger only targets creatures defending player controls")
    void onlyTargetsDefendingPlayerCreatures() {
        addCreatureReady(player1, new PurifyingDragon());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }
}
