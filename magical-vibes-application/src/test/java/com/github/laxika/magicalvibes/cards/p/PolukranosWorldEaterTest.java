package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.v.VoyagingSatyr;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PolukranosWorldEater.class, VoyagingSatyr.class})
class PolukranosWorldEaterTest extends BaseCardTest {

    @Test
    void monstrosityUsesPaidXAndDividesDamageBeforePlayersCanRespond() {
        Permanent polukranos = addReadyPolukranos(player1);
        Permanent firstBears = addReadyCreature(player2);
        Permanent secondBears = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, firstBears.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, secondBears.getId());

        PendingInteraction.XValueChoice allocation =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(allocation).isNotNull();
        assertThat(allocation.minValue()).isEqualTo(1);
        assertThat(allocation.maxValue()).isEqualTo(1);

        harness.handleXValueChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 1);

        assertThat(firstBears.getMarkedDamage()).isZero();
        assertThat(secondBears.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(polukranos.isMonstrous()).isTrue();
        assertThat(polukranos.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(firstBears.getId())
                        && permanent.getMarkedDamage() == 1)
                .anyMatch(permanent -> permanent.getId().equals(secondBears.getId())
                        && permanent.getMarkedDamage() == 1);
    }

    @Test
    void alreadyMonstrousCreatureCanActivateAgainButNothingHappens() {
        Permanent polukranos = addReadyPolukranos(player1);
        polukranos.setMonstrous(true);
        polukranos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void zeroXBecomesMonstrousWithoutTargetingOrDamage() {
        Permanent polukranos = addReadyPolukranos(player1);
        Permanent creature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(polukranos.isMonstrous()).isTrue();
        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(polukranos.getMarkedDamage()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void positiveXRequiresAtLeastOneTargetWhenAnOpponentHasACreature() {
        addReadyPolukranos(player1);
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).doesNotContain(player1.getId());
    }

    private Permanent addReadyPolukranos(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new PolukranosWorldEater());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new VoyagingSatyr());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
