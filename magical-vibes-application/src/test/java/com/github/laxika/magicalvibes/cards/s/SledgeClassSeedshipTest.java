package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SledgeClassSeedship.class, GrizzlyBears.class})
class SledgeClassSeedshipTest extends BaseCardTest {

    @Test
    @DisplayName("Station puts counters equal to the tapped creature's power on Sledge-Class Seedship")
    void stationUsesTappedCreaturePower() {
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(seedship), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(seedship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("At seven charge counters, Sledge-Class Seedship becomes a flying artifact creature")
    void sevenCountersAnimateAndGrantFlying() {
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());

        seedship.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, seedship)).isFalse();
        assertThat(gqs.hasKeyword(gd, seedship, Keyword.FLYING)).isFalse();

        seedship.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, seedship)).isTrue();
        assertThat(gqs.hasKeyword(gd, seedship, Keyword.FLYING)).isTrue();

        seedship.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, seedship)).isFalse();
        assertThat(gqs.hasKeyword(gd, seedship, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("When Sledge-Class Seedship attacks, it may put a creature from hand onto the battlefield")
    void attackPutsCreatureFromHandOntoBattlefield() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Permanent seedship = addReadySeedship();
        seedship.setCounterCount(CounterType.CHARGE, 7);

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Station requires another untapped creature")
    void stationNeedsAnotherUntappedCreature() {
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(seedship), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySeedship() {
        Permanent seedship = new Permanent(new SledgeClassSeedship());
        seedship.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(seedship);
        return seedship;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(battlefieldIndex(findPermanent(player1, "Sledge-Class Seedship"))));
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
