package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InscriptionOfAbundance.class, GrizzlyBears.class, HillGiant.class})
class InscriptionOfAbundanceTest extends BaseCardTest {

    @Test
    void putsTwoPlusOnePlusOneCountersOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCard();

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0}, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void targetPlayerGainsLifeEqualToTheirGreatestCreaturePower() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 10);
        prepareCard();

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{1}, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 12);
        harness.assertLife(player1, 20);
    }

    @Test
    void fightsAControlledCreatureAgainstAnOpposingCreature() {
        Permanent fighter = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{2},
                List.of(fighter.getId(), opponent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponent);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fighter);
    }

    @Test
    void kickerAllowsAllThreeModesAndUsesTheFightModeTargetGroups() {
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fighter = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new InscriptionOfAbundance()));
        addMana(2, 3);

        gs.playCard(gd, player1, 0,
                com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.encodeModeSelection(1, 3, new int[]{0, 1, 2}),
                null, null, List.of(counterTarget.getId(), player2.getId(), fighter.getId(), opponent.getId()),
                List.of(), false, null, null, null, null, null, true);
        harness.passBothPriorities();

        assertThat(counterTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player2, 12);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponent);
    }

    @Test
    void cannotChooseMultipleModesWithoutKicker() {
        prepareCard();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 3, new int[]{0, 1}, List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedSpellCanChooseNoModes() {
        harness.setHand(player1, List.of(new InscriptionOfAbundance()));
        addMana(2, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(), false, null, null, null, null, null, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Inscription of Abundance");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void kickedModesUseCountersForLifeGainAndFightInPrintedOrder() {
        Permanent fighter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new InscriptionOfAbundance()));
        addMana(2, 3);

        gs.playCard(gd, player1, 0,
                com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.encodeModeSelection(1, 3, new int[]{0, 1, 2}),
                null, null, List.of(fighter.getId(), player1.getId(), fighter.getId(), opponent.getId()),
                List.of(), false, null, null, null, null, null, true);
        harness.passBothPriorities();

        assertThat(fighter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 14);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(fighter.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void lifeModeGainsNoLifeWhenTargetPlayerControlsNoCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLife(player2, 10);
        prepareCard();

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{1}, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
    }

    @Test
    void counterModeCanTargetAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0}, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void remainingLifeModeResolvesWhenFightTargetLeavesBattlefield() {
        Permanent fighter = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new InscriptionOfAbundance()));
        addMana(2, 3);

        gs.playCard(gd, player1, 0,
                com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.encodeModeSelection(1, 3, new int[]{1, 2}),
                null, null, List.of(player1.getId(), fighter.getId(), opponent.getId()),
                List.of(), false, null, null, null, null, null, true);
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        gd.playerGraveyards.get(player2.getId()).add(opponent.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(fighter.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new InscriptionOfAbundance()));
        addMana(1, 1);
    }

    private void addMana(int green, int colorless) {
        harness.addMana(player1, ManaColor.GREEN, green);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
