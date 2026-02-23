package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhispersilkCloakTest extends BaseCardTest {


    // ===== Card properties =====


    @Test
    @DisplayName("Whispersilk Cloak has static CantBeBlockedEffect and GrantKeywordEffect(SHROUD)")
    void hasStaticEffects() {
        WhispersilkCloak card = new WhispersilkCloak();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof CantBeBlockedEffect)
                .hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof GrantKeywordEffect gke
                        && gke.keyword() == Keyword.SHROUD
                        && gke.scope() == GrantKeywordEffect.Scope.EQUIPPED_CREATURE)
                .hasSize(1);
    }

    @Test
    @DisplayName("Whispersilk Cloak has equip {2} ability with correct properties")
    void hasEquipAbility() {
        WhispersilkCloak card = new WhispersilkCloak();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Whispersilk Cloak puts it on the battlefield unattached")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new WhispersilkCloak()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Whispersilk Cloak")
                        && p.getAttachedTo() == null);
    }

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches Cloak to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Shroud: equipped creature has shroud =====

    @Test
    @DisplayName("Equipped creature has shroud")
    void equippedCreatureHasShroud() {
        Permanent creature = addReadyCreature(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Creature loses shroud when Cloak is removed")
    void creatureLosesShroudWhenCloakRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(cloak);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Unequipped creature does not have shroud from Cloak")
    void unequippedCreatureDoesNotHaveShroud() {
        Permanent creature = addReadyCreature(player1);
        addCloakReady(player1); // not attached

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    // ===== Can't be blocked: equipped creature can't be blocked =====

    @Test
    @DisplayName("Equipped creature can't be blocked")
    void equippedCreatureCantBeBlocked() {
        Permanent creature = addReadyCreature(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Creature loses can't-be-blocked when Cloak is removed")
    void creatureLosesCantBeBlockedWhenCloakRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(cloak);

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("Unequipped creature is not unblockable from Cloak")
    void unequippedCreatureIsNotUnblockable() {
        Permanent creature = addReadyCreature(player1);
        addCloakReady(player1); // not attached

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
    }

    // ===== Combat: equipped creature attacks unblocked =====

    @Test
    @DisplayName("Equipped creature attacks and cannot be assigned blockers")
    void equippedCreatureCannotBeBlocked() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addReadyCreature(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // Player2 has a creature that could block but won't be offered
        addReadyCreature(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Attacker (2/2) should deal damage unblocked
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Cloak can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        cloak.setAttachedTo(creature1.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature1)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, creature1)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature2)).isTrue();
    }

    // ===== Helpers =====

    private Permanent addCloakReady(Player player) {
        Permanent perm = new Permanent(new WhispersilkCloak());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
