package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorgonFlail.class, RuneclawBear.class, CrawWurm.class})
class GorgonFlailTest extends BaseCardTest {

    @Test
    @DisplayName("Equip costs two mana and does not tap the equipment")
    void equipPaysTwoManaWithoutTapping() {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(flail.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting Gorgon Flail puts it on the battlefield unattached")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GorgonFlail()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gorgon Flail")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Gorgon Flail to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);   // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Equipped creature has deathtouch")
    void equippedCreatureHasDeathtouch() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Creature loses deathtouch when Gorgon Flail is removed")
    void creatureLosesDeathtouchWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(flail);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature with deathtouch destroys any creature it damages in combat")
    void deathtouchDestroysBlocker() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CrawWurm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Re-equipping Gorgon Flail moves it to new creature")
    void reEquipMovesToNewCreature() {
        Permanent flail = addFlailReady(player1);
        Permanent creature1 = addCreatureReady(player1, new RuneclawBear());
        Permanent creature2 = addCreatureReady(player1, new RuneclawBear());

        flail.setAttachedTo(creature1.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(3);
        // First creature loses deathtouch
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.DEATHTOUCH)).isFalse();
        // Second creature gains deathtouch
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent flail = addFlailReady(player1);
        Permanent opponent = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(flail.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void cannotEquipNoncreature() {
        Permanent flail = addFlailReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flail.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(flail.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(flail.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Failed re-equip leaves the equipment on its original creature")
    void disappearingTargetPreservesOriginalAttachment() {
        Permanent flail = addFlailReady(player1);
        Permanent original = addCreatureReady(player1, new RuneclawBear());
        Permanent target = addCreatureReady(player1, new RuneclawBear());
        flail.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, original, Keyword.DEATHTOUCH)).isTrue();
    }

    private Permanent addFlailReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GorgonFlail());
    }
}
