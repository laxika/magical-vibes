package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureXEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KessigWolfRunTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Kessig Wolf Run has two activated abilities")
    void hasTwoActivatedAbilities() {
        KessigWolfRun card = new KessigWolfRun();

        assertThat(card.getActivatedAbilities()).hasSize(2);
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{X}{R}{G}");
        assertThat(card.getActivatedAbilities().get(1).getEffects())
                .hasSize(2)
                .satisfies(effects -> {
                    assertThat(effects.get(0)).isInstanceOf(BoostTargetCreatureXEffect.class);
                    assertThat(effects.get(1)).isInstanceOf(GrantKeywordEffect.class);
                });
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tappingForManaAddsColorless() {
        Permanent landPerm = addKessigWolfRun(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(landPerm.isTapped()).isTrue();
    }

    // ===== Pump ability =====

    @Test
    @DisplayName("Activating pump ability puts it on the stack")
    void activatingPumpAbilityPutsItOnStack() {
        addKessigWolfRun(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 2, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Kessig Wolf Run");
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetPermanentId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Resolving pump ability grants +X/+0 and trample")
    void resolvingPumpAbilityGrantsBoostAndTrample() {
        addKessigWolfRun(player1);
        Permanent creature = addCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, 3, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Resolving pump ability with X=0 still grants trample")
    void resolvingWithXZeroStillGrantsTrample() {
        addKessigWolfRun(player1);
        Permanent creature = addCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Mana is consumed when activating pump ability")
    void manaIsConsumedWhenActivatingPumpAbility() {
        addKessigWolfRun(player1);
        Permanent creature = addCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 2, creature.getId());

        // X=2 + {R} + {G} = 4 mana total
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Pump ability taps the land")
    void pumpAbilityTapsTheLand() {
        Permanent landPerm = addKessigWolfRun(player1);
        Permanent creature = addCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, 0, creature.getId());

        assertThat(landPerm.isTapped()).isTrue();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentsCreature() {
        addKessigWolfRun(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot target non-creature permanent")
    void cannotTargetNonCreature() {
        addKessigWolfRun(player1);
        // Add another land as a non-creature target
        KessigWolfRun otherLand = new KessigWolfRun();
        Permanent landPerm = new Permanent(otherLand);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(landPerm);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, landPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Pump ability fizzles if target is removed before resolution")
    void pumpAbilityFizzlesIfTargetRemoved() {
        addKessigWolfRun(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 2, creature.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate pump ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addKessigWolfRun(player1);
        Permanent creature = addCreature(player1);
        // Only add red, no green
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate pump ability when land is already tapped")
    void cannotActivateWhenTapped() {
        Permanent landPerm = addKessigWolfRun(player1);
        landPerm.tap();
        Permanent creature = addCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Helper methods =====

    private Permanent addKessigWolfRun(Player player) {
        KessigWolfRun card = new KessigWolfRun();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreature(Player player) {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
