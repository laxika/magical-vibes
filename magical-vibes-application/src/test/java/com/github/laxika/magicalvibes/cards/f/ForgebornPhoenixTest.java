package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.w.WebspinnerCuff;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForgebornPhoenix.class, GrizzlyBears.class, LightningBolt.class, WebspinnerCuff.class})
class ForgebornPhoenixTest extends BaseCardTest {

    @Test
    void equippedCreatureGainsFlyingAndPhoenixStopsBeingACreature() {
        Permanent phoenix = addReadyPhoenix();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(phoenix.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, phoenix)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(phoenix.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, phoenix)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent phoenix = addReadyPhoenix();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(phoenix.getAttachedTo()).isNull();
    }

    @Test
    void phoenixReturnsTappedAfterItsOwnDeathWhenAnEquippedCreatureDealsCombatDamage() {
        Permanent phoenix = addReadyPhoenix();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(attacker);
        destroyPhoenix(phoenix);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Forgeborn Phoenix");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void phoenixReturnsWhenAnEquippedCreatureDies() {
        Permanent phoenix = addReadyPhoenix();
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        phoenix.setAttachedTo(dyingCreature.getId());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(attacker);

        dyingCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        destroyPhoenix(phoenix);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Forgeborn Phoenix").isTapped()).isTrue();
    }

    @Test
    void phoenixReturnsAfterAnEquippedCreatureDealsCombatDamageToAPlaneswalker() {
        Permanent phoenix = addReadyPhoenix();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(attacker);
        destroyPhoenix(phoenix);

        Permanent planeswalker = addTestPlaneswalker();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(attackerIndex), Map.of(attackerIndex, planeswalker.getId()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(planeswalker.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY))
                .isEqualTo(1);
        assertThat(findPermanent(player1, "Forgeborn Phoenix").isTapped()).isTrue();
    }

    private Permanent addReadyPhoenix() {
        return addCreatureReady(player1, new ForgebornPhoenix());
    }

    private void attachEquipment(Permanent creature) {
        Permanent equipment = addCreatureReady(player1, new WebspinnerCuff());
        equipment.setAttachedTo(creature.getId());
    }

    private void destroyPhoenix(Permanent phoenix) {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, phoenix.getId());
        resolveAllTriggers();
    }

    private Permanent addTestPlaneswalker() {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        return planeswalker;
    }
}
