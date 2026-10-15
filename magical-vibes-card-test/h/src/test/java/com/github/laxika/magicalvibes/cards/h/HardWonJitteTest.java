package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PurpleDragonPunks;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HardWonJitte.class, PurpleDragonPunks.class})
class HardWonJitteTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has double strike")
    void equippedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new PurpleDragonPunks());
        Permanent jitte = addCreatureReady(player1, new HardWonJitte());
        jitte.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses double strike when Hard-Won Jitte is removed")
    void creatureLosesDoubleStrikeWhenJitteIsRemoved() {
        Permanent creature = addCreatureReady(player1, new PurpleDragonPunks());
        Permanent jitte = addCreatureReady(player1, new HardWonJitte());
        jitte.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(jitte);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Hard-Won Jitte to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent jitte = addCreatureReady(player1, new HardWonJitte());
        Permanent creature = addCreatureReady(player1, new PurpleDragonPunks());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jitte.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by an opponent")
    void equipCannotTargetOpponentCreature() {
        addCreatureReady(player1, new HardWonJitte());
        Permanent opponentCreature = addCreatureReady(player2, new PurpleDragonPunks());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void reequippingMovesDoubleStrikeToNewCreature() {
        Permanent jitte = addCreatureReady(player1, new HardWonJitte());
        Permanent original = addCreatureReady(player1, new PurpleDragonPunks());
        Permanent replacement = addCreatureReady(player1, new PurpleDragonPunks());
        jitte.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, replacement.getId());
        assertThat(jitte.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(jitte.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        addCreatureReady(player1, new HardWonJitte());
        Permanent creature = addCreatureReady(player1, new PurpleDragonPunks());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void failedReequipLeavesOriginalCreatureEquipped() {
        Permanent jitte = addCreatureReady(player1, new HardWonJitte());
        Permanent original = addCreatureReady(player1, new PurpleDragonPunks());
        Permanent replacement = addCreatureReady(player1, new PurpleDragonPunks());
        jitte.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, replacement.getId());
        gd.playerBattlefields.get(player1.getId()).remove(replacement);
        harness.passBothPriorities();

        assertThat(jitte.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.DOUBLE_STRIKE)).isTrue();
    }
    @Test
    void equippedCreatureDealsDamageInBothCombatDamageSteps() {
        Permanent creature = addCreatureReady(player1, new PurpleDragonPunks());
        Permanent jitte = addCreatureReady(player1, new HardWonJitte());
        jitte.setAttachedTo(creature.getId());

        declareAttackers(java.util.List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    void equipCannotBeActivatedOnOpponentsTurn() {
        addCreatureReady(player1, new HardWonJitte());
        Permanent creature = addCreatureReady(player1, new PurpleDragonPunks());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void equipCannotBeActivatedWhileStackIsNonempty() {
        addCreatureReady(player1, new HardWonJitte());
        Permanent creature = addCreatureReady(player1, new PurpleDragonPunks());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent jitte = addCreatureReady(player1, new HardWonJitte());
        Permanent creature = addCreatureReady(player1, new PurpleDragonPunks());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jitte.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
