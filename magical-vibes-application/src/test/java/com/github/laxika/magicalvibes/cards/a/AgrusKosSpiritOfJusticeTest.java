package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgrusKosSpiritOfJustice.class, GrizzlyBears.class, Plains.class})
class AgrusKosSpiritOfJusticeTest extends BaseCardTest {

    @Test
    void entersAndSuspectsAnUnsuspectedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAgrus(target.getId());

        assertThat(target.isSuspected()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void entersAndExilesAnAlreadySuspectedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setSuspected(true);

        castAgrus(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void attackingSuspectsAnUnsuspectedTarget() {
        addCreatureReady(player1, new AgrusKosSpiritOfJustice());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isSuspected()).isTrue();
    }

    @Test
    void attackingExilesAnAlreadySuspectedCreature() {
        addCreatureReady(player1, new AgrusKosSpiritOfJustice());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgrusKosSpiritOfJustice());
        target.setSuspected(true);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void attackTriggerChecksSuspectedStatusAtResolution() {
        addCreatureReady(player1, new AgrusKosSpiritOfJustice());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgrusKosSpiritOfJustice());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        target.setSuspected(true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void attackTriggerSuspectsCreatureWhoseSuspectedStatusWasRemoved() {
        addCreatureReady(player1, new AgrusKosSpiritOfJustice());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgrusKosSpiritOfJustice());
        target.setSuspected(true);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        target.setSuspected(false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.isSuspected()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void canSuspectItselfWhenAttacking() {
        Permanent agrus = addCreatureReady(player1, new AgrusKosSpiritOfJustice());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, agrus.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(agrus);
        assertThat(agrus.isSuspected()).isTrue();
        assertThat(agrus.isTapped()).isFalse();
    }

    @Test
    void canSuspectItselfWhenEnteringWithoutBeingCast() {
        Permanent agrus = harness.enterBattlefieldAndReturn(player1, new AgrusKosSpiritOfJustice());

        harness.handlePermanentChosen(player1, agrus.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(agrus);
        assertThat(agrus.isSuspected()).isTrue();
    }

    @Test
    void canDeclineTheOptionalTarget() {
        harness.setHand(player1, List.of(new AgrusKosSpiritOfJustice()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Agrus Kos, Spirit of Justice")).hasSize(1);
    }

    @Test
    void cannotTargetAland() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new AgrusKosSpiritOfJustice()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castAgrus(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AgrusKosSpiritOfJustice()));
        addMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
