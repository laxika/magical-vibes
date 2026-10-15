package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
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

@CardUsed({VowOfDuty.class, VampireNoble.class, JaceBeleren.class, InvasionOfZendikar.class})
class VowOfDutyTest extends BaseCardTest {

    @Test
    @DisplayName("Vow of Duty gives the enchanted creature +2/+2 and vigilance")
    void grantsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new VampireNoble());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VowOfDuty());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot attack the Aura controller")
    void enchantedCreatureCannotAttackAuraController() {
        Permanent creature = addCreatureReady(player1, new VampireNoble());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfDuty());
        aura.setAttachedTo(creature.getId());

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(0), Map.of(0, player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot attack the Aura controller's planeswalker")
    void enchantedCreatureCannotAttackAuraControllersPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new VampireNoble());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfDuty());
        aura.setAttachedTo(creature.getId());
        Permanent planeswalker = addPlaneswalker(player2);

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The restriction affects only the enchanted creature")
    void doesNotRestrictOtherCreatures() {
        Permanent enchanted = addCreatureReady(player1, new VampireNoble());
        addCreatureReady(player1, new VampireNoble());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfDuty());
        aura.setAttachedTo(enchanted.getId());

        declareAttackers(player1, List.of(1));
    }

    @Test
    @DisplayName("The restriction ends when Vow of Duty leaves the battlefield")
    void restrictionEndsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new VampireNoble());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfDuty());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        declareAttackers(player1, List.of(0));
    }

    @Test
    void resolvesAttachedToAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new VampireNoble());
        harness.setHand(player1, List.of(new VowOfDuty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Vow of Duty");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, player1.getId()))
                .isFalse();
    }

    @Test
    void cannotEnchantAPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2);
        harness.setHand(player1, List.of(new VowOfDuty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, planeswalker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotEnterWhenItsTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new VampireNoble());
        harness.setHand(player1, List.of(new VowOfDuty()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vow of Duty");
        harness.assertInGraveyard(player1, "Vow of Duty");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictionFollowsTheCurrentAuraController() {
        Permanent creature = addCreatureReady(player1, new VampireNoble());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfDuty());
        aura.setAttachedTo(creature.getId());
        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, player2.getId()))
                .isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, player2.getId()))
                .isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void creatureCanAttackAnotherPlayerWithoutTapping() {
        Permanent creature = addCreatureReady(player1, new VampireNoble());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VowOfDuty());
        aura.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void boostAndVigilanceEndWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new VampireNoble());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VowOfDuty());
        aura.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void canAttackABattleControlledByTheAuraController() {
        Permanent creature = addCreatureReady(player1, new VampireNoble());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VowOfDuty());
        aura.setAttachedTo(creature.getId());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());

        beginAttack(player1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId())));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.getAttackTarget()).isEqualTo(battle.getId());
        assertThat(creature.isTapped()).isFalse();
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new JaceBeleren());
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        return permanent;
    }
}
