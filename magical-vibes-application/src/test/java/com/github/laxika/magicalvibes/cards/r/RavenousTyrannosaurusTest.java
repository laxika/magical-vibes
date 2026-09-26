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
    @DisplayName("Devour 3 puts three +1/+1 counters on it per sacrificed creature")
    void devourPutsCountersOnEntry() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new RavenousTyrannosaurus())));
        addManaForRavenousTyrannosaurus();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        Permanent tyrannosaurus = findPermanent(player1, "Ravenous Tyrannosaurus");
        assertThat(tyrannosaurus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(fodderA.getId())
                        || permanent.getId().equals(fodderB.getId()));
    }

    @Test
    @DisplayName("Attacking deals its power to another creature and excess to that creature's controller")
    void attackDealsExcessDamageToController() {
        addCreatureReady(player1, new RavenousTyrannosaurus());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.playerLifeTotals.put(player2.getId(), 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("The attack target is optional")
    void attackCanDeclineTarget() {
        addCreatureReady(player1, new RavenousTyrannosaurus());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The attack trigger cannot target Ravenous Tyrannosaurus itself")
    void attackCannotTargetItself() {
        Permanent tyrannosaurus = addCreatureReady(player1, new RavenousTyrannosaurus());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, tyrannosaurus.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForRavenousTyrannosaurus() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
