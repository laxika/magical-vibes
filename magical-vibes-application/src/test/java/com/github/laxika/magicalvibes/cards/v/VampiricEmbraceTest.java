package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArcLightning;
import com.github.laxika.magicalvibes.cards.e.Expunge;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampiricEmbrace.class, GorillaWarrior.class, HermeticStudy.class,
        ArcLightning.class, Expunge.class, Forest.class})
class VampiricEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2, flying, and a counter when a creature it damaged dies")
    void enchantedCreatureGetsBoostFlyingAndCounterWhenDamagedCreatureDies() {
        Permanent enchantedCreature = addCreatureReady(player1, new GorillaWarrior());
        attachAura(enchantedCreature);
        Permanent blocker = addCreatureReady(player2, new GorillaWarrior());

        enchantedCreature.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.FLYING)).isTrue();

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gorilla Warrior");
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not grant a counter when the damaged creature survives")
    void noCounterWhenDamagedCreatureSurvives() {
        Permanent enchantedCreature = addCreatureReady(player1, new GorillaWarrior());
        attachAura(enchantedCreature);
        GorillaWarrior blockerCard = new GorillaWarrior();
        blockerCard.setPower(1);
        blockerCard.setToughness(8);
        Permanent blocker = addCreatureReady(player2, blockerCard);

        enchantedCreature.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Gorilla Warrior");
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers when a creature damaged by the enchanted creature dies later that turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent enchantedCreature = addCreatureReady(player1, new GorillaWarrior());
        attachAura(enchantedCreature);

        GorillaWarrior toughBlockerCard = new GorillaWarrior();
        toughBlockerCard.setPower(1);
        toughBlockerCard.setToughness(8);
        Permanent blocker = addCreatureReady(player2, toughBlockerCard);

        enchantedCreature.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Gorilla Warrior");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(blocker.getId(), 3));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gorilla Warrior");
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when the damaged creature dies on a later turn")
    void doesNotTriggerWhenDamagedCreatureDiesOnLaterTurn() {
        Permanent enchantedCreature = addCreatureReady(player1, new GorillaWarrior());
        attachAura(enchantedCreature);

        GorillaWarrior toughBlockerCard = new GorillaWarrior();
        toughBlockerCard.setPower(1);
        toughBlockerCard.setToughness(8);
        Permanent blocker = addCreatureReady(player2, toughBlockerCard);

        enchantedCreature.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Gorilla Warrior");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setHand(player1, List.of(new Expunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gorilla Warrior");
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enchanted creature's noncombat damage also triggers the counter ability")
    void triggersForNoncombatDamageFromEnchantedCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new GorillaWarrior());
        attachAura(enchantedCreature);
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(enchantedCreature.getId());

        GorillaWarrior targetCard = new GorillaWarrior();
        targetCard.setToughness(1);
        Permanent target = addCreatureReady(player2, targetCard);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gorilla Warrior");
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage from another source does not trigger the counter ability")
    void doesNotTriggerForDamageFromAnotherSource() {
        Permanent enchantedCreature = addCreatureReady(player1, new GorillaWarrior());
        attachAura(enchantedCreature);
        Permanent target = addCreatureReady(player2, new GorillaWarrior());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, Map.of(target.getId(), 3));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gorilla Warrior");
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter is placed on an enchanted creature controlled by an opponent")
    void countersOpponentControlledEnchantedCreature() {
        Permanent enchantedCreature = addCreatureReady(player2, new GorillaWarrior());
        attachAura(player1, enchantedCreature);
        Permanent blocker = addCreatureReady(player1, new GorillaWarrior());

        enchantedCreature.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat(player2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gorilla Warrior");
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can only enchant a creature")
    void cannotEnchantNonCreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new VampiricEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent forest = findPermanent(player1, "Forest");
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachAura(Permanent creature) {
        attachAura(player1, creature);
    }

    private void attachAura(Player auraController, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new VampiricEmbrace());
        aura.setAttachedTo(creature.getId());
    }
}
