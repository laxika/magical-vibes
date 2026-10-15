package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.ForceAway;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonThroneOfTarkir.class, GrizzlyBears.class, ForceAway.class})
class DragonThroneOfTarkirTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has defender and the granted tap ability")
    void equippedCreatureGetsStaticAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent throne = addThroneReady(player1);
        throne.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability boosts other creatures you control and gives them trample")
    void grantedAbilityBoostsOtherOwnCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent throne = addThroneReady(player1);
        throne.setAttachedTo(creature.getId());
        creature.setPowerModifier(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The granted boost wears off at end of turn")
    void grantedBoostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent throne = addThroneReady(player1);
        throne.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equip attaches the Throne and moving it removes defender from the previous creature")
    void equipMovesTheGrantedAbilities() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent throne = addThroneReady(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 2, null, first.getId());
        harness.passBothPriorities();
        assertThat(throne.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEFENDER)).isTrue();

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();
        assertThat(throne.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("X uses the creature's power at resolution and stays fixed afterwards")
    void powerIsDeterminedAtResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addThroneReady(player1).setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        creature.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(7);
        creature.setPowerModifier(0);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(7);
        Permanent lateCreature = addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Negative power gives no boost but still grants trample")
    void negativePowerStillGrantsTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addThroneReady(player1).setAttachedTo(creature.getId());
        creature.setPowerModifier(-3);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The activated ability resolves even after the Throne becomes unattached")
    void losingEquipmentDoesNotStopTheAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent throne = addThroneReady(player1);
        throne.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        throne.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A removed source uses its power immediately before leaving the battlefield")
    void removedCreatureUsesLastKnownPower() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addThroneReady(player1).setAttachedTo(creature.getId());
        harness.setHand(player2, java.util.List.of(new ForceAway()));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        creature.setPowerModifier(3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addThroneReady(Player player) {
        return addCreatureReady(player, new DragonThroneOfTarkir());
    }
}
