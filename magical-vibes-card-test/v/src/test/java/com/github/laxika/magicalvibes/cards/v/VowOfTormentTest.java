package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VowOfTorment.class, GrizzlyBears.class, JaceBeleren.class, InvasionOfZendikar.class})
class VowOfTormentTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsPlusTwoPlusTwoAndMenace() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfTorment());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    void enchantedCreatureCannotAttackAuraController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfTorment());
        aura.setAttachedTo(creature.getId());

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(0), Map.of(0, player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void enchantedCreatureCannotAttackAuraControllersPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfTorment());
        aura.setAttachedTo(creature.getId());
        Permanent planeswalker = addPlaneswalker(player2);

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void effectsStopWhenAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfTorment());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addPlaneswalker(Player player) {
        return harness.addToBattlefieldAndReturn(player, new JaceBeleren());
    }

    @Test
    void castingAttachesToOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VowOfTorment()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Vow of Torment").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    void cannotEnchantPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2);
        harness.setHand(player1, List.of(new VowOfTorment()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, planeswalker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void menaceRejectsOneBlockerButAllowsTwo() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VowOfTorment());
        aura.setAttachedTo(creature.getId());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void attackRestrictionTracksAuraControllerAndEndsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new VowOfTorment());
        aura.setAttachedTo(creature.getId());
        assertThat(als.canAttackDefender(gd, creature, player2.getId())).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerBattlefields.get(player1.getId()).add(aura);
        assertThat(als.canAttackDefender(gd, creature, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, creature, player1.getId())).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        assertThat(als.canAttackDefender(gd, creature, player1.getId())).isTrue();
    }

    @Test
    void enchantedCreatureCanAttackBattleControlledByAuraController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VowOfTorment());
        aura.setAttachedTo(creature.getId());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());

        assertThat(als.getValidAttackTargetIds(gd, player1.getId())).contains(battle.getId());
        assertThat(als.canAttackDefender(gd, creature, battle.getId())).isTrue();
    }
}
