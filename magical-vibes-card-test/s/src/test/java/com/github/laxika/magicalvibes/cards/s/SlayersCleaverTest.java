package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrownyardBehemoth;
import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.w.WretchedGryff;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlayersCleaver.class, FieldCreeper.class, DrownyardBehemoth.class, WretchedGryff.class})
class SlayersCleaverTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent cleaver = addReadyCleaver(player1);
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped attacker must be blocked by an Eldrazi if one can block")
    void requiresEldraziBlockerIfAble() {
        Permanent attacker = addReadyCreature(player1);
        Permanent cleaver = addReadyCleaver(player1);
        cleaver.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        addReadyCreature(player2);
        addReadyCreature(player2, CardSubtype.ELDRAZI);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("matching creature");
    }

    @Test
    @DisplayName("One Eldrazi blocker satisfies the requirement")
    void oneEldraziBlockerIsEnough() {
        Permanent attacker = addReadyCreature(player1);
        Permanent cleaver = addReadyCleaver(player1);
        cleaver.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        addReadyCreature(player2);
        Permanent eldrazi = addReadyCreature(player2, CardSubtype.ELDRAZI);
        addReadyCreature(player2, CardSubtype.ELDRAZI);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(eldrazi.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("No Eldrazi blocker is required when none can block")
    void noEldraziBlockerIsRequiredWhenNoneCanBlock() {
        Permanent attacker = addReadyCreature(player1);
        Permanent cleaver = addReadyCleaver(player1);
        cleaver.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent eldrazi = addReadyCreature(player2, CardSubtype.ELDRAZI);
        eldrazi.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(eldrazi.isBlocking()).isFalse();
    }

    @Test
    void equipPaysFourManaAndAttachesOnResolution() {
        Permanent cleaver = addReadyCleaver(player1);
        Permanent creature = addCreatureReady(player1, new FieldCreeper());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(cleaver.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void equipCannotBePaidWithThreeMana() {
        Permanent cleaver = addReadyCleaver(player1);
        Permanent creature = addCreatureReady(player1, new FieldCreeper());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cleaver.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent cleaver = addReadyCleaver(player1);
        Permanent creature = addCreatureReady(player2, new FieldCreeper());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cleaver.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetNoncreature() {
        Permanent cleaver = addReadyCleaver(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cleaver.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent cleaver = addReadyCleaver(player1);
        Permanent creature = addCreatureReady(player1, new FieldCreeper());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cleaver.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedWithNonemptyStack() {
        addReadyCleaver(player1);
        Permanent creature = addCreatureReady(player1, new FieldCreeper());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void reequippingMovesBonusesOnlyOnResolution() {
        Permanent cleaver = addReadyCleaver(player1);
        Permanent first = addCreatureReady(player1, new FieldCreeper());
        Permanent second = addCreatureReady(player1, new FieldCreeper());
        cleaver.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(cleaver.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void unattachedCleaverDoesNotGrantBoostOrBlockingRequirement() {
        Permanent attacker = addCreatureReady(player1, new FieldCreeper());
        addReadyCleaver(player1);
        Permanent eldrazi = addCreatureReady(player2, new DrownyardBehemoth());
        attacker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        assertThat(eldrazi.isBlocking()).isFalse();
    }

    @Test
    void noEldraziAllowsNonEldraziBlocker() {
        Permanent attacker = addCreatureReady(player1, new FieldCreeper());
        addReadyCleaver(player1).setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new FieldCreeper());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void groundEldraziCannotBeRequiredToBlockFlyingAttacker() {
        Permanent attacker = addCreatureReady(player1, new WretchedGryff());
        addReadyCleaver(player1).setAttachedTo(attacker.getId());
        Permanent eldrazi = addCreatureReady(player2, new DrownyardBehemoth());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(eldrazi.isBlocking()).isFalse();
    }

    @Test
    void twoCleaversOnOneAttackerNeedOnlyOneEldraziBlocker() {
        Permanent attacker = addCreatureReady(player1, new FieldCreeper());
        addReadyCleaver(player1).setAttachedTo(attacker.getId());
        addReadyCleaver(player1).setAttachedTo(attacker.getId());
        Permanent eldrazi = addCreatureReady(player2, new DrownyardBehemoth());
        attacker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(eldrazi.isBlocking()).isTrue();
    }

    @Test
    void oneEldraziMayBlockEitherOfTwoEquippedAttackers() {
        Permanent first = addCreatureReady(player1, new FieldCreeper());
        Permanent second = addCreatureReady(player1, new FieldCreeper());
        addReadyCleaver(player1).setAttachedTo(first.getId());
        addReadyCleaver(player1).setAttachedTo(second.getId());
        Permanent eldrazi = addCreatureReady(player2, new DrownyardBehemoth());
        first.setAttacking(true);
        second.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(eldrazi.isBlocking()).isTrue();
        assertThat(eldrazi.getBlockingTargetIds()).containsExactly(first.getId());
    }

    private Permanent addReadyCleaver(Player player) {
        return addCreatureReady(player, new SlayersCleaver());
    }

    private Permanent addReadyCreature(Player player, CardSubtype... subtypes) {
        return addCreatureReady(player, createCreature(subtypes));
    }

    private static Card createCreature(CardSubtype... subtypes) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }
}
