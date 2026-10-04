package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExecutionersHood.class, DawntreaderElk.class, BlackCat.class, Ornithopter.class})
class ExecutionersHoodTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Executioner's Hood and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ExecutionersHood()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Executioner's Hood")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Executioner's Hood to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hood.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature has intimidate")
    void equippedCreatureHasIntimidate() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        hood.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses intimidate when Executioner's Hood is removed")
    void creatureLosesIntimidateWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        hood.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(hood);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Executioner's Hood does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        Permanent otherCreature = addCreatureReady(player1, new DawntreaderElk());
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        hood.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Executioner's Hood can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        Permanent creature1 = addCreatureReady(player1, new DawntreaderElk());
        Permanent creature2 = addCreatureReady(player1, new DawntreaderElk());

        hood.setAttachedTo(creature1.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INTIMIDATE)).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(hood.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INTIMIDATE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.INTIMIDATE)).isTrue();
    }


    @Test
    void equipRequiresTwoMana() {
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(hood.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        Permanent creature = addCreatureReady(player2, new DawntreaderElk());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(hood.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetNoncreature() {
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hood.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(hood.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(hood.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedHoodCanEquipSummoningSickCreature() {
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        hood.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hood.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(hood.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    void intimidateAllowsSharedColorOrArtifactBlockers() {
        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        hood.setAttachedTo(attacker.getId());
        Permanent greenBlocker = addCreatureReady(player2, new DawntreaderElk());
        Permanent blackBlocker = addCreatureReady(player2, new BlackCat());
        Permanent artifactBlocker = addCreatureReady(player2, new Ornithopter());

        assertThat(bls.canBlockAttacker(gd, greenBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, blackBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, artifactBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void failedReEquipKeepsOriginalAttachmentAndIntimidate() {
        Permanent hood = harness.addToBattlefieldAndReturn(player1, new ExecutionersHood());
        Permanent original = addCreatureReady(player1, new DawntreaderElk());
        Permanent target = addCreatureReady(player1, new DawntreaderElk());
        hood.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(hood.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.INTIMIDATE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
