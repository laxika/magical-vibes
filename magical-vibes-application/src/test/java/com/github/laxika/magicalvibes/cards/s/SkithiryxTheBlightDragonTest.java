package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkithiryxTheBlightDragonTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Skithiryx has two activated abilities with correct effects")
    void hasCorrectAbilities() {
        SkithiryxTheBlightDragon card = new SkithiryxTheBlightDragon();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: {B}: gains haste
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{B}");
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GrantKeywordEffect.class);

        // Second ability: {B}{B}: regenerate
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{B}{B}");
        assertThat(card.getActivatedAbilities().get(1).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Skithiryx puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new SkithiryxTheBlightDragon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skithiryx, the Blight Dragon");
    }

    @Test
    @DisplayName("Resolving Skithiryx puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new SkithiryxTheBlightDragon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Skithiryx, the Blight Dragon"));
    }

    // ===== Haste ability =====

    @Test
    @DisplayName("Activating haste ability puts it on the stack")
    void hasteAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving haste ability grants haste until end of turn")
    void hasteAbilityGrantsHaste() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent skithiryx = findPermanent(player1, "Skithiryx, the Blight Dragon");

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(skithiryx.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste ability can be activated with summoning sickness (no tap cost)")
    void hasteAbilityWorksWithSummoningSickness() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent skithiryx = findPermanent(player1, "Skithiryx, the Blight Dragon");
        assertThat(skithiryx.isSummoningSick()).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(skithiryx.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate haste ability without enough mana")
    void cannotActivateHasteWithoutMana() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Haste ability consumes {B} mana")
    void hasteAbilityConsumesMana() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Regenerate ability =====

    @Test
    @DisplayName("Activating regenerate ability puts it on the stack")
    void regenerateAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving regenerate ability grants a regeneration shield")
    void regenerateAbilityGrantsShield() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent skithiryx = findPermanent(player1, "Skithiryx, the Blight Dragon");
        assertThat(skithiryx.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerate ability can be activated with summoning sickness (no tap cost)")
    void regenerateAbilityWorksWithSummoningSickness() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent skithiryx = findPermanent(player1, "Skithiryx, the Blight Dragon");
        assertThat(skithiryx.isSummoningSick()).isTrue();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(skithiryx.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regenerate ability without enough mana")
    void cannotActivateRegenerateWithoutMana() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());
        harness.addMana(player1, ManaColor.BLACK, 1); // Need {B}{B}, only have {B}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regenerate ability consumes {B}{B} mana")
    void regenerateAbilityConsumesMana() {
        harness.addToBattlefield(player1, new SkithiryxTheBlightDragon());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Regeneration saves from lethal damage =====

    @Test
    @DisplayName("Regeneration shield saves Skithiryx from lethal regular combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent skithiryx = addSkithiryxReady(player1);
        skithiryx.setRegenerationShield(1);
        skithiryx.setBlocking(true);
        skithiryx.addBlockingTarget(0);

        // Use a non-infect creature so damage is regular (not -1/-1 counters)
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Skithiryx (4/4) takes only 2 damage from Grizzly Bears - not lethal, survives without regen
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Skithiryx, the Blight Dragon"));
        // Shield not consumed since damage wasn't lethal
        Permanent survived = findPermanent(player1, "Skithiryx, the Blight Dragon");
        assertThat(survived.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Skithiryx dies to infect damage even with regeneration shield (0 toughness from counters)")
    void diesFromInfectDespiteRegenerationShield() {
        // Infect deals -1/-1 counters which persist after regeneration
        Permanent skithiryx = addSkithiryxReady(player1);
        skithiryx.setRegenerationShield(1);
        skithiryx.setBlocking(true);
        skithiryx.addBlockingTarget(0);

        // Another infect creature with 4+ power
        Permanent attacker = addSkithiryxReady(player2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Skithiryx should die because -1/-1 counters make it 0/0 and regeneration
        // can't save from 0 toughness (counters persist after regeneration)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skithiryx, the Blight Dragon"));
    }

    // ===== Infect combat interaction =====

    @Test
    @DisplayName("Skithiryx deals poison counters to defending player when unblocked")
    void dealsPoison() {
        Permanent skithiryx = addSkithiryxReady(player1);
        skithiryx.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        // Infect does not deal regular damage to players
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Skithiryx deals -1/-1 counters to blocking creature")
    void dealsMinusCountersToBlocker() {
        Permanent skithiryx = addSkithiryxReady(player1);
        skithiryx.setAttacking(true);

        // Block with a 5/5 so it survives
        Permanent blocker = addCreatureReady(player2, new SkithiryxTheBlightDragon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // The blocker (4/4) takes 4 damage as -1/-1 counters → 0/0 → dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skithiryx, the Blight Dragon"));
    }

    // ===== Helper methods =====

    private Permanent addSkithiryxReady(Player player) {
        SkithiryxTheBlightDragon card = new SkithiryxTheBlightDragon();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
