package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PettyTheft;
import com.github.laxika.magicalvibes.cards.q.QueenOfIce;
import com.github.laxika.magicalvibes.cards.r.RageOfWinter;
import com.github.laxika.magicalvibes.cards.t.TomeRaider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrazenBorrower.class, PettyTheft.class, QueenOfIce.class, RageOfWinter.class, Island.class, TomeRaider.class})
class BrazenBorrowerTest extends BaseCardTest {

    @Test
    void adventureReturnsTargetNonlandPermanentAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QueenOfIce());
        BrazenBorrower card = new BrazenBorrower();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(harness.getGameData().playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        BrazenBorrower card = new BrazenBorrower();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockOnlyCreaturesWithFlying() {
        Permanent blocker = addCreatureReady(player2, new BrazenBorrower());
        Permanent attacker = addCreatureReady(player1, new QueenOfIce());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void canBlockCreatureWithFlying() {
        Permanent blocker = addCreatureReady(player2, new BrazenBorrower());
        Permanent attacker = addCreatureReady(player1, new TomeRaider());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void adventureCannotTargetYourOwnNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QueenOfIce());
        harness.setHand(player1, List.of(new BrazenBorrower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureWithTargetNowControlledByCasterGoesToGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QueenOfIce());
        BrazenBorrower card = new BrazenBorrower();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureReturnsOpponentControlledPermanentToItsOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QueenOfIce());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.setHand(player1, List.of(new BrazenBorrower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void canCastCreatureFromExileAfterAdventureResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QueenOfIce());
        BrazenBorrower card = new BrazenBorrower();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(card));
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void creatureCanBeCastWhileAdventureIsOnStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QueenOfIce());
        BrazenBorrower adventure = new BrazenBorrower();
        BrazenBorrower creature = new BrazenBorrower();
        harness.setHand(player1, List.of(adventure, creature));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, target.getId());

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(creature));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.findExiledCard(adventure.getId())).isNotNull();
    }

    @Test
    void flyingAttackerCannotBeBlockedByCreatureWithoutFlyingOrReach() {
        Permanent attacker = addCreatureReady(player1, new BrazenBorrower());
        addCreatureReady(player2, new QueenOfIce());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
