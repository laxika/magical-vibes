package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnappingCreeper;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BasiliskCollar.class, GrizzlyBears.class, SnappingCreeper.class})
class BasiliskCollarTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has deathtouch and lifelink")
    void equippedCreatureHasKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent collar = addBasiliskCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses deathtouch and lifelink when Basilisk Collar is removed")
    void creatureLosesKeywordsWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent collar = addBasiliskCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(collar);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Basilisk Collar to a creature you control")
    void equipsToControlledCreature() {
        Permanent collar = addBasiliskCollarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Basilisk Collar does not grant keywords to an unequipped creature")
    void doesNotAffectUnequippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addBasiliskCollarReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void equippedCreatureKillsBlockerWithDeathtouchAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new SnappingCreeper());
        Permanent collar = addBasiliskCollarReady(player1);
        collar.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new SnappingCreeper());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player2, "Snapping Creeper");
        harness.assertOnBattlefield(player1, "Snapping Creeper");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedEquippedCreatureGainsLifeForCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new SnappingCreeper());
        Permanent collar = addBasiliskCollarReady(player1);
        collar.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void reequippingTransfersBothKeywordsOnlyWhenAbilityResolves() {
        Permanent collar = addBasiliskCollarReady(player1);
        Permanent original = addCreatureReady(player1, new SnappingCreeper());
        Permanent target = addCreatureReady(player1, new SnappingCreeper());
        collar.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(collar.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, original, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent collar = addBasiliskCollarReady(player1);
        Permanent target = addCreatureReady(player2, new SnappingCreeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(collar.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent collar = addBasiliskCollarReady(player1);
        Permanent target = addCreatureReady(player1, new SnappingCreeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(collar.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBasiliskCollarReady(Player player) {
        return addCreatureReady(player, new BasiliskCollar());
    }
}
