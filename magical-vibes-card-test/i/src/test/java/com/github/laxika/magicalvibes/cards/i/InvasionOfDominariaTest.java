package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KhenraSpellspear;
import com.github.laxika.magicalvibes.cards.s.SerraFaithkeeper;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, InvasionOfDominaria.class, KhenraSpellspear.class, SerraFaithkeeper.class})
class InvasionOfDominariaTest extends BaseCardTest {

    @Test
    @DisplayName("When Invasion of Dominaria enters, you gain 4 life and draw a card")
    void entersGainsLifeAndDrawsCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.getLife(player1.getId());

        castInvasion();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Defeat exiles the Siege and casts Serra Faithkeeper transformed")
    void defeatCastsBackFace() {
        castInvasion();

        Permanent battle = findPermanent(player1, "Invasion of Dominaria");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.passBothPriorities();

        Permanent faithkeeper = findPermanent(player1, "Serra Faithkeeper");
        assertThat(faithkeeper.isTransformed()).isTrue();
    }

    @Test
    void controllerCanDeclineCastingTheDefeatedSiege() {
        castInvasion();
        Permanent battle = findPermanent(player1, "Invasion of Dominaria");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Invasion of Dominaria");
        harness.assertNotOnBattlefield(player1, "Serra Faithkeeper");
        assertThat(gd.findExiledCard(battle.getCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entryAbilityResolvesAfterSiegeLeavesBattlefield() {
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castFromHand(player1, new InvasionOfDominaria(), "{2}{W}");
        harness.passBothPriorities();
        Permanent battle = findPermanent(player1, "Invasion of Dominaria");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, battle));

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    void castingFaithkeeperDoesNotTriggerProwess() {
        castInvasion();
        Permanent spellspear = harness.addToBattlefieldAndReturn(player1, new KhenraSpellspear());
        int powerBefore = gqs.getEffectivePower(gd, spellspear);
        Permanent battle = findPermanent(player1, "Invasion of Dominaria");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Serra Faithkeeper");
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(powerBefore);
    }

    @Test
    void faithkeeperFliesOverGroundCreatureAndAttacksWithoutTapping() {
        castInvasion();
        Permanent battle = findPermanent(player1, "Invasion of Dominaria");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();
        Permanent faithkeeper = findPermanent(player1, "Serra Faithkeeper");
        faithkeeper.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new KhenraSpellspear());
        int opponentLifeBefore = gd.getLife(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(faithkeeper.isTapped()).isFalse();
        assertThat(bls.canBlockAttacker(gd, blocker, faithkeeper,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 4);
    }

    @Test
    void defeatedSiegeIsCastFromExileRatherThanGraveyard() {
        castInvasion();
        int exileCastsBefore = gd.getSpellsCastThisTurnCount(player1.getId(), Zone.EXILE);
        int graveyardCastsBefore = gd.getSpellsCastThisTurnCount(player1.getId(), Zone.GRAVEYARD);
        Permanent battle = findPermanent(player1, "Invasion of Dominaria");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Serra Faithkeeper");
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId(), Zone.EXILE))
                .isEqualTo(exileCastsBefore + 1);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId(), Zone.GRAVEYARD))
                .isEqualTo(graveyardCastsBefore);
    }

    private void castInvasion() {
        harness.castFromHand(player1, new InvasionOfDominaria(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
