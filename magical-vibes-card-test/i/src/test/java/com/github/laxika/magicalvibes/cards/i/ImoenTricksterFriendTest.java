package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImoenTricksterFriend.class, GrizzlyBears.class, Plains.class, Island.class, Shock.class, DoomBlade.class})
class ImoenTricksterFriendTest extends BaseCardTest {

    @Test
    void cannotBeBlockedWhenAttackingAlone() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent imoen = addCreatureReady(player1, new ImoenTricksterFriend());
        imoen.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(imoen);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void reducedSpecializeExilesAnInstantAndPutsCountersOnCreatures() {
        Permanent imoen = addCreatureReady(player1, new ImoenTricksterFriend());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Shock shock = new Shock();
        DoomBlade doomBlade = new DoomBlade();
        harness.setGraveyard(player1, List.of(shock, doomBlade));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(imoen.getCard().getName()).isEqualTo("Imoen, Honorable Trickster");
        assertThat(imoen.getCard().getPower()).isEqualTo(3);

        imoen.setAttacking(true);
        imoen.setAttackTarget(player2.getId());
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getId())
                .contains(shock.getId());
        assertThat(imoen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blueFaceTargetsAnOpponentsCreatureAfterExiling() {
        Permanent imoen = addCreatureReady(player1, new ImoenTricksterFriend());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock, new DoomBlade()));
        harness.setHand(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        imoen.setAttacking(true);
        imoen.setAttackTarget(player2.getId());
        resolveCombat();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    void canBeBlockedWhenAnotherCreatureIsAttacking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent imoen = addCreatureReady(player1, new ImoenTricksterFriend());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        imoen.setAttacking(true);
        bear.setAttacking(true);
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(imoen)))));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void blackFaceCreatesZombieDuringTheExileAbilityResolution() {
        Permanent imoen = specialize(2, new DoomBlade());
        exileAfterCombatDamage(imoen);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    void redFaceDealsDamageDuringTheExileAbilityResolution() {
        Permanent imoen = specialize(3, new Shock());
        harness.setLife(player2, 20);
        exileAfterCombatDamage(imoen);
        harness.assertLife(player2, 15);
    }

    @Test
    void greenFaceDrawsAndAllowsAnExtraLandDuringTheExileAbilityResolution() {
        Permanent imoen = specialize(4, new GrizzlyBears());
        Island drawn = new Island();
        harness.setLibrary(player1, List.of(drawn, new Island(), new Island()));
        exileAfterCombatDamage(imoen);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Island(), new Plains()));
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void decliningExileDoesNotPutCountersOnCreatures() {
        Permanent imoen = specialize(0, new Plains());
        imoen.setAttacking(true);
        imoen.setAttackTarget(player2.getId());
        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(imoen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void blueFaceCanExileWithoutAnOpponentCreatureToTarget() {
        Permanent imoen = specialize(1, new Island());
        exileAfterCombatDamage(imoen);
    }

    @Test
    void specializeRequiresFiveManaWithOnlyOneInstantInGraveyard() {
        addCreatureReady(player1, new ImoenTricksterFriend());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private Permanent specialize(int abilityIndex, Card discarded) {
        Permanent imoen = addCreatureReady(player1, new ImoenTricksterFriend());
        harness.setGraveyard(player1, List.of(new Shock(), new DoomBlade()));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        return imoen;
    }

    private void exileAfterCombatDamage(Permanent imoen) {
        imoen.setAttacking(true);
        imoen.setAttackTarget(player2.getId());
        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Card instant = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(instant);
    }
}
