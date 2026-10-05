package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.d.DeadlyAlliance;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalakirRebirth.class, MalakirMire.class, DeadlyAlliance.class, Forest.class, CliffhavenSellSword.class})
class MalakirRebirthTest extends BaseCardTest {

    @Test
    void losesLifeAndReturnsTargetCreatureTappedWhenItDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        var creatureCard = creature.getCard();

        castRebirth(creature);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);

        destroy(player2, creature);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    void returnsOpponentsCreatureUnderItsOwnersControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        var creatureCard = creature.getCard();

        castRebirth(creature);

        destroy(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    void grantedDeathTriggerWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        var creatureCard = creature.getCard();

        castRebirth(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroy(player2, creature);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    void returnsStolenCreatureToOwnerRatherThanController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        castRebirth(creature);

        destroy(player1, creature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cliffhaven Sell-Sword");
        harness.assertNotOnBattlefield(player2, "Cliffhaven Sell-Sword");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void losesNoLifeWhenTargetDiesBeforeSpellResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new MalakirRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, 0, creature.getId());

        destroy(player2, creature);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Cliffhaven Sell-Sword");
        harness.assertNotOnBattlefield(player1, "Cliffhaven Sell-Sword");
    }

    @Test
    void returnedCreatureDoesNotRetainGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        castRebirth(creature);
        destroy(player2, creature);
        harness.passBothPriorities();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();

        destroy(player2, returned);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cliffhaven Sell-Sword");
        harness.assertNotOnBattlefield(player1, "Cliffhaven Sell-Sword");
    }

    @Test
    void olderDeathTriggerCannotReturnCardAfterItReturnsAndDiesAgain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        castRebirth(creature);
        castRebirth(creature);
        destroy(player2, creature);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();

        destroy(player2, returned);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cliffhaven Sell-Sword");
        harness.assertNotOnBattlefield(player1, "Cliffhaven Sell-Sword");
    }

    @Test
    void spellFaceCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MalakirRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landFaceEntersTappedAndProducesBlackMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MalakirRebirth()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(MalakirMire.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void castRebirth(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MalakirRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();
    }

    private void destroy(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DeadlyAlliance()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
