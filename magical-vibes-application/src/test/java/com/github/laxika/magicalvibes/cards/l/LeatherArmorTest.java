package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeatherArmor.class, GrizzlyBears.class, GiantGrowth.class, ProdigalSorcerer.class})
class LeatherArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+1 and ward {1}")
    void equippedCreatureGetsToughnessAndWard() {
        Permanent creature = addCreatureReady(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        prepareOpponentTurn();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Paying ward lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent creature = addCreatureReady(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability when they do not pay")
    void wardCountersUnpaidAbility() {
        Permanent creature = addCreatureReady(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        prepareOpponentTurn();

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(sorcerer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equip can be activated only once each turn")
    void equipCanBeActivatedOnlyOnceEachTurn() {
        Permanent armor = addArmorReady(player1);
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, firstCreature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, secondCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    private Permanent addArmorReady(Player player) {
        Permanent armor = harness.addToBattlefieldAndReturn(player, new LeatherArmor());
        armor.setSummoningSick(false);
        return armor;
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }



    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
