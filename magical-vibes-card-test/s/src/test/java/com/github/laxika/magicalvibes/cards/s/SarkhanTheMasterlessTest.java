package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DisciplesOfTheInferno;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.i.InvasionOfRegatha;
import com.github.laxika.magicalvibes.cards.w.WardscaleCrocodile;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SarkhanTheMasterless.class, NarsetParterOfVeils.class, WardscaleCrocodile.class,
        ImprisonedInTheMoon.class, InvasionOfRegatha.class, DisciplesOfTheInferno.class, Silhouette.class})
class SarkhanTheMasterlessTest extends BaseCardTest {

    @Test
    @DisplayName("+1 animates each planeswalker you control as a red Dragon with flying")
    void plusOneAnimatesControlledPlaneswalkers() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        Permanent narset = harness.addToBattlefieldAndReturn(player1, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertAnimatedDragon(sarkhan);
        assertAnimatedDragon(narset);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(narset.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-3 creates a 4/4 red Dragon token with flying")
    void minusThreeCreatesDragon() {
        Permanent sarkhan = addReadySarkhan(player1, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getCard().getPower()).isEqualTo(4);
        assertThat(dragon.getCard().getToughness()).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, dragon)).containsExactly(CardColor.RED);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger deals one damage for each Dragon to the attacking creature")
    void dragonsDamageAttacker() {
        addReadySarkhan(player1, 7);
        createDragonTokens(2);
        Permanent attacker = addCreatureReady(player2, new WardscaleCrocodile());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger also fires when a creature attacks a planeswalker")
    void attackOnPlaneswalkerTriggersAbility() {
        addReadySarkhan(player1, 5);
        Permanent narset = harness.addToBattlefieldAndReturn(player1, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 3);
        createDragonToken();
        Permanent attacker = addCreatureReady(player2, new WardscaleCrocodile());

        declareAttackers(player2, List.of(0), Map.of(0, narset.getId()));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 leaves opposing planeswalkers and ordinary creatures unchanged")
    void plusOneOnlyAnimatesOwnPlaneswalkers() {
        addReadySarkhan(player1, 5);
        Permanent opposingNarset = harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils());
        Permanent crocodile = addCreatureReady(player1, new WardscaleCrocodile());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isPlaneswalker(gd, opposingNarset)).isTrue();
        assertThat(gqs.isCreature(gd, opposingNarset)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, crocodile)).doesNotContain(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, crocodile, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Planeswalkers entering after +1 resolves are not animated")
    void animationDoesNotAffectLaterPlaneswalkers() {
        addReadySarkhan(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent narset = harness.addToBattlefieldAndReturn(player1, new NarsetParterOfVeils());

        assertThat(gqs.isPlaneswalker(gd, narset)).isTrue();
        assertThat(gqs.isCreature(gd, narset)).isFalse();
    }

    @Test
    @DisplayName("Animated planeswalkers retain their loyalty abilities")
    void animatedNarsetCanActivateLoyaltyAbility() {
        addReadySarkhan(player1, 5);
        Permanent narset = harness.addToBattlefieldAndReturn(player1, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(narset.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertAnimatedDragon(narset);
    }

    @Test
    @DisplayName("Animation expires at the end of the turn")
    void animationExpiresAtEndOfTurn() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        Permanent narset = harness.addToBattlefieldAndReturn(player1, new NarsetParterOfVeils());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        for (Permanent permanent : List.of(sarkhan, narset)) {
            assertThat(gqs.isPlaneswalker(gd, permanent)).isTrue();
            assertThat(gqs.isCreature(gd, permanent)).isFalse();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isFalse();
            assertThat(gqs.effectiveCreatureSubtypes(gd, permanent)).doesNotContain(CardSubtype.DRAGON);
        }
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("A planeswalker that entered this turn cannot attack after animation")
    void newlyEnteredAnimatedSarkhanCannotAttack() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        sarkhan.setSummoningSick(true);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying the last three loyalty still creates the Dragon")
    void minusThreeWithExactlyThreeLoyalty() {
        addReadySarkhan(player1, 3);

        createDragonToken();

        harness.assertInGraveyard(player1, "Sarkhan the Masterless");
        harness.assertNotOnBattlefield(player1, "Sarkhan the Masterless");
        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 cannot be activated with fewer than three loyalty counters")
    void minusThreeRequiresEnoughLoyalty() {
        Permanent sarkhan = addReadySarkhan(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(countPermanents(player1, "Dragon")).isZero();
    }

    @Test
    @DisplayName("The attack trigger fires separately for every attacker and ignores non-Dragons")
    void eachAttackerIsDamagedOnlyByDragons() {
        addReadySarkhan(player1, 5);
        createDragonToken();
        addCreatureReady(player1, new WardscaleCrocodile());
        Permanent first = addCreatureReady(player2, new WardscaleCrocodile());
        Permanent second = addCreatureReady(player2, new WardscaleCrocodile());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger counts Dragons when it resolves, not when it triggers")
    void dragonRemovedBeforeResolutionDealsNoDamage() {
        addReadySarkhan(player1, 5);
        createDragonToken();
        Permanent dragon = findPermanent(player1, "Dragon");
        Permanent attacker = addCreatureReady(player2, new WardscaleCrocodile());
        declareAttackers(player2, List.of(0));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dragon));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An already-triggered ability still resolves after Sarkhan leaves")
    void triggerSurvivesSarkhanLeavingBattlefield() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        createDragonToken();
        Permanent attacker = addCreatureReady(player2, new WardscaleCrocodile());
        declareAttackers(player2, List.of(0));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sarkhan));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sarkhan does not trigger after Imprisoned in the Moon removes his abilities")
    void noAttackTriggerAfterLosingAbilities() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        createDragonToken();
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, sarkhan.getId());
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player2, new WardscaleCrocodile());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Disciples of the Inferno does not increase damage dealt by Dragons")
    void noncreatureDamageBonusDoesNotApplyToDragons() {
        addReadySarkhan(player1, 5);
        createDragonToken();
        harness.addToBattlefield(player1, new InvasionOfRegatha().getBackFaceCard());
        Permanent attacker = addCreatureReady(player2, new WardscaleCrocodile());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Wardscale Crocodile");
    }

    @Test
    @DisplayName("Silhouette cannot prevent damage from Sarkhan's non-targeting trigger")
    void silhouetteDoesNotPreventDragonDamage() {
        addReadySarkhan(player1, 5);
        createDragonToken();
        Permanent attacker = addCreatureReady(player2, new WardscaleCrocodile());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Silhouette()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, attacker.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lethal Dragon damage destroys the attacker before combat damage")
    void dragonsKillAttackerBeforeCombatDamage() {
        addReadySarkhan(player1, 10);
        createDragonTokens(3);
        addCreatureReady(player2, new WardscaleCrocodile());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Wardscale Crocodile");
        harness.assertNotOnBattlefield(player2, "Wardscale Crocodile");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Attacking a battle does not trigger Sarkhan, including a battle you control")
    void attackingBattleDoesNotTriggerSarkhan() {
        addReadySarkhan(player1, 5);
        createDragonToken();
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        battle.setProtectorPlayerId(player2.getId());
        Permanent attacker = addCreatureReady(player1, new WardscaleCrocodile());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(3), Map.of(3, battle.getId()));
            resolveAllTriggers();
        });

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void assertAnimatedDragon(Permanent planeswalker) {
        assertThat(gqs.isCreature(gd, planeswalker)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, planeswalker)).isFalse();
        assertThat(gqs.getEffectivePower(gd, planeswalker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, planeswalker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, planeswalker, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, planeswalker)).containsExactly(CardColor.RED);
        assertThat(gqs.effectiveCreatureSubtypes(gd, planeswalker)).contains(CardSubtype.DRAGON);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addReadySarkhan(Player player, int loyalty) {
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, loyalty);
        sarkhan.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return sarkhan;
    }

    private void createDragonToken() {
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

    private void createDragonTokens(int count) {
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
                harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
            }
            createDragonToken();
        }
    }
}
