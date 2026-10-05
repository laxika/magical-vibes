package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MerfolkSecretkeeper;
import com.github.laxika.magicalvibes.cards.r.RageOfWinter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QueenOfIce.class, RageOfWinter.class, GiantSpider.class, MerfolkSecretkeeper.class, Island.class})
class QueenOfIceTest extends BaseCardTest {

    @Test
    void adventureTapsTargetCreatureAndLocksItsNextUntapStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        QueenOfIce card = new QueenOfIce();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void combatDamageToCreatureTapsItAndLocksItsNextUntapStep() {
        Permanent queen = addCreatureReady(player1, new QueenOfIce());
        queen.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void adventureCannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        QueenOfIce card = new QueenOfIce();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void combatDamageCreatesOneAbilityContainingBothEffects() {
        Permanent queen = addCreatureReady(player1, new QueenOfIce());
        Permanent blocker = addCreatureReady(player2, new MerfolkSecretkeeper());
        queen.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(queen.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    void adventureLocksAlreadyTappedCreatureOnlyForItsControllersNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MerfolkSecretkeeper());
        target.setTapped(true);
        harness.setHand(player1, List.of(new QueenOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void adventureWithAnIllegalTargetGoesToGraveyardInsteadOfExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MerfolkSecretkeeper());
        QueenOfIce card = new QueenOfIce();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAdventure(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void combatDamageToPlayerDoesNotCreateTheCreatureDamageAbility() {
        Permanent queen = addCreatureReady(player1, new QueenOfIce());
        queen.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MerfolkSecretkeeper());
        QueenOfIce card = new QueenOfIce();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertOnBattlefield(player1, "Queen of Ice");
    }
}
