package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disarm;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RingOfXathrid.class, ElvishVisionary.class, WalkingCorpse.class, Murder.class, Disarm.class})
class RingOfXathridTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {1} attaches the Ring to target creature you control")
    void equipAttachesToCreature() {
        Permanent ring = addRingReady(player1);
        Permanent creature = addCreatureReady(player1, new ElvishVisionary());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("{2} ability grants a regeneration shield to the equipped creature")
    void abilityRegeneratesEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new ElvishVisionary());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("{2} ability does nothing while the Ring is unattached")
    void abilityDoesNothingWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new ElvishVisionary());
        addRingReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger puts a +1/+1 counter on a black equipped creature")
    void upkeepAddsCounterToBlackCreature() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Upkeep trigger does nothing when the equipped creature is not black")
    void upkeepDoesNothingForNonBlackCreature() {
        Permanent creature = addCreatureReady(player1, new ElvishVisionary());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger does nothing while the Ring is unattached")
    void upkeepDoesNothingWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        addRingReady(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Regeneration prevents destruction and consumes its shield")
    void regenerationPreventsMurder() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(ring.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Unattaching the Ring before resolution prevents regeneration")
    void regenerationDoesNothingIfDisarmedInResponse() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of(new Disarm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(ring.getAttachedTo()).isNull();
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration uses the creature equipped at resolution")
    void regenerationUsesCurrentAttachment() {
        Permanent original = addCreatureReady(player1, new WalkingCorpse());
        Permanent current = addCreatureReady(player1, new ElvishVisionary());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 2, 0, null, null);
        ring.setAttachedTo(current.getId());
        harness.passBothPriorities();

        assertThat(original.getRegenerationShield()).isZero();
        assertThat(current.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A resolved regeneration shield survives the Ring becoming unattached")
    void resolvedShieldSurvivesDisarm() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Disarm(), new Murder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(ring.getAttachedTo()).isNull();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The Ring's controller adds a counter even if an opponent controls the equipped creature")
    void upkeepAddsCounterToOpponentControlledCreature() {
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Ring does not add counters during its opponent's upkeep")
    void opponentUpkeepDoesNotAddCounter() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger checks the equipped creature's color at resolution")
    void upkeepChecksCurrentEquippedCreature() {
        Permanent greenCreature = addCreatureReady(player1, new ElvishVisionary());
        Permanent blackCreature = addCreatureReady(player1, new WalkingCorpse());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(greenCreature.getId());

        advanceToUpkeep(player1);
        ring.setAttachedTo(blackCreature.getId());
        harness.passBothPriorities();

        assertThat(greenCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(blackCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addRingReady(Player player) {
        return addCreatureReady(player, new RingOfXathrid());
    }
}
