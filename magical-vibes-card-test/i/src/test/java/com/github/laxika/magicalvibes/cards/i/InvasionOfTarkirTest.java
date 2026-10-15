package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DefiantThundermaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RavenousSailback;
import com.github.laxika.magicalvibes.cards.z.ZurgoAndOjutai;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefiantThundermaw.class, ZurgoAndOjutai.class, Forest.class, RavenousSailback.class,
        InvasionOfTarkir.class})
class InvasionOfTarkirTest extends BaseCardTest {

    @Test
    void etbRevealsDragonsBeforeChoosingAnOtherTarget() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new RavenousSailback());
        ZurgoAndOjutai dragon = new ZurgoAndOjutai();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new InvasionOfTarkir(), dragon, forest));
        addManaToCast();

        gs.playCard(gd, player1, 0, 0, null, null);
        resolveAllTriggers();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(reveal.validCardIds()).containsExactly(dragon.getId());

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        PendingInteraction.PermanentChoice target =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        Permanent battle = findPermanent(player1, "Invasion of Tarkir");
        assertThat(target.validIds()).contains(targetCreature.getId()).doesNotContain(battle.getId());
        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void etbDealsTwoDamageWhenNoDragonIsRevealed() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new RavenousSailback());
        harness.castFromHand(player1, new InvasionOfTarkir(), "{1}{R}");
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void transformedFaceDealsDamageWhenADragonAttacks() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfTarkir());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Defiant Thundermaw")).isNotNull();
        Permanent dragon = addCreatureReady(player1, new ZurgoAndOjutai());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new RavenousSailback());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragon)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.getMarkedDamage()).isEqualTo(2);
    }

    private void addManaToCast() {
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
    }

    @Test
    void revealingMultipleDragonsCountsCardsAndLeavesThemInHand() {
        ZurgoAndOjutai first = new ZurgoAndOjutai();
        ZurgoAndOjutai second = new ZurgoAndOjutai();
        ZurgoAndOjutai unrevealed = new ZurgoAndOjutai();
        harness.setHand(player1, List.of(new InvasionOfTarkir(), first, second, unrevealed));
        addManaToCast();
        gs.playCard(gd, player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, unrevealed);
    }

    @Test
    void mayRevealZeroEvenWithADragonInHandAndTargetYourself() {
        ZurgoAndOjutai dragon = new ZurgoAndOjutai();
        harness.setHand(player1, List.of(new InvasionOfTarkir(), dragon));
        addManaToCast();
        gs.playCard(gd, player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dragon);
    }

    @Test
    void attackingDragonIsTheDamageSource() {
        Permanent thundermaw = harness.addToBattlefieldAndReturn(player1, new DefiantThundermaw());
        Permanent dragon = addCreatureReady(player1, new ZurgoAndOjutai());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RavenousSailback());
        harness.addToBattlefield(player2, new DefiantThundermaw());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragon)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.damageDealtThisTurnBySource.getOrDefault(dragon.getId(), 0)).isEqualTo(2);
        assertThat(gd.damageDealtThisTurnBySource.getOrDefault(thundermaw.getId(), 0)).isZero();
    }

    @Test
    void thundermawTriggersForItsOwnAttack() {
        Permanent thundermaw = addCreatureReady(player1, new DefiantThundermaw());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RavenousSailback());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(thundermaw)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void nonDragonAttackDoesNotTriggerThundermaw() {
        harness.addToBattlefield(player1, new DefiantThundermaw());
        Permanent attacker = addCreatureReady(player1, new RavenousSailback());
        harness.addToBattlefield(player2, new RavenousSailback());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void defeatedSiegeMayBeLeftInExileWithoutCastingItsBackFace() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfTarkir());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Invasion of Tarkir");
        harness.assertNotOnBattlefield(player1, "Defiant Thundermaw");
        assertThat(gd.findExiledCard(battle.getOriginalCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

}
