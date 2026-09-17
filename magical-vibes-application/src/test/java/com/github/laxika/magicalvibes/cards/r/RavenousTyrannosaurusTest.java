package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousTyrannosaurus.class, GrizzlyBears.class})
class RavenousTyrannosaurusTest extends BaseCardTest {

    @Test
    @DisplayName("Devouring two creatures gives six +1/+1 counters")
    void devourTwoAddsSixCounters() {
        Permanent fodderA = addCreatureReady(player1, new GrizzlyBears());
        Permanent fodderB = addCreatureReady(player1, new GrizzlyBears());

        castRavenousTyrannosaurus();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        Permanent tyrannosaurus = findPermanent(player1, "Ravenous Tyrannosaurus");
        assertThat(tyrannosaurus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("The attack trigger deals excess damage to the target creature's controller")
    void attackTriggerDealsExcessDamageToCreatureController() {
        Permanent tyrannosaurus = addCreatureReady(player1, new RavenousTyrannosaurus());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 30);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(tyrannosaurus.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The attack trigger cannot target Ravenous Tyrannosaurus itself")
    void attackTriggerCannotTargetItself() {
        Permanent tyrannosaurus = addCreatureReady(player1, new RavenousTyrannosaurus());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, tyrannosaurus.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger can resolve without a target")
    void attackTriggerCanResolveWithoutTarget() {
        addCreatureReady(player1, new RavenousTyrannosaurus());
        harness.setLife(player2, 30);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Tyrannosaurus");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    private void castRavenousTyrannosaurus() {
        harness.setHand(player1, new ArrayList<>(List.of(new RavenousTyrannosaurus())));
        addMana();
        harness.castCreature(player1, 0);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
