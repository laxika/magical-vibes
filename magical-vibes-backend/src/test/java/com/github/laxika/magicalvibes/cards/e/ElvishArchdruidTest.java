package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvishArchdruidTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Elvish Archdruid has static boost and tap mana ability")
    void hasCorrectProperties() {
        ElvishArchdruid card = new ElvishArchdruid();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AddManaPerControlledPermanentEffect.class);
    }

    // ===== Static effect: buffs other Elves you control =====

    @Test
    @DisplayName("Other Elf creatures you control get +1/+1")
    void buffsOtherOwnElves() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new ElvishArchdruid());

        Permanent elf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Elvish Archdruid does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ElvishArchdruid());

        Permanent archdruid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Archdruid"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, archdruid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, archdruid)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Elf creatures")
    void doesNotBuffNonElves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ElvishArchdruid());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Elf creatures")
    void doesNotBuffOpponentElves() {
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent opponentElf = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Elvish Archdruids buff each other")
    void twoArchdruidsBuffEachOther() {
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new ElvishArchdruid());

        List<Permanent> archdruids = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Archdruid"))
                .toList();

        assertThat(archdruids).hasSize(2);
        for (Permanent archdruid : archdruids) {
            assertThat(gqs.getEffectivePower(gd, archdruid)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, archdruid)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Two Elvish Archdruids give +2/+2 to other Elves")
    void twoArchdruidsStackBonuses() {
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new LlanowarElves());

        Permanent elf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Elvish Archdruid leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new LlanowarElves());

        Permanent elf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Elvish Archdruid"));

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
    }

    // ===== Tap ability: Add {G} for each Elf you control =====

    @Test
    @DisplayName("Tap ability adds G for each Elf you control including itself")
    void tapAbilityAddsGreenManaPerElf() {
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());

        Permanent archdruid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Archdruid"))
                .findFirst().orElseThrow();
        archdruid.setSummoningSick(false);

        int archdruidIdx = gd.playerBattlefields.get(player1.getId()).indexOf(archdruid);
        harness.activateAbility(player1, archdruidIdx, null, null);

        // Mana ability resolves immediately (no stack). 3 Elves: Archdruid + 2 Llanowar Elves = 3 green mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tap ability with only Archdruid on battlefield adds 1 G")
    void tapAbilityWithOnlyArchdruidAddsOneGreen() {
        harness.addToBattlefield(player1, new ElvishArchdruid());

        Permanent archdruid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Archdruid"))
                .findFirst().orElseThrow();
        archdruid.setSummoningSick(false);

        int archdruidIdx = gd.playerBattlefields.get(player1.getId()).indexOf(archdruid);
        harness.activateAbility(player1, archdruidIdx, null, null);

        // Mana ability resolves immediately (no stack). Only Archdruid itself = 1 green mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability does not count opponent's Elves")
    void tapAbilityDoesNotCountOpponentElves() {
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent archdruid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Archdruid"))
                .findFirst().orElseThrow();
        archdruid.setSummoningSick(false);

        int archdruidIdx = gd.playerBattlefields.get(player1.getId()).indexOf(archdruid);
        harness.activateAbility(player1, archdruidIdx, null, null);

        // Mana ability resolves immediately. Only Archdruid itself = 1 green mana (opponent's Elves not counted)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability does not count non-Elf creatures")
    void tapAbilityDoesNotCountNonElves() {
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent archdruid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Archdruid"))
                .findFirst().orElseThrow();
        archdruid.setSummoningSick(false);

        int archdruidIdx = gd.playerBattlefields.get(player1.getId()).indexOf(archdruid);
        harness.activateAbility(player1, archdruidIdx, null, null);

        // Mana ability resolves immediately. Only Archdruid itself = 1 green mana (Grizzly Bears not counted)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability cannot be activated with summoning sickness")
    void tapAbilityBlockedBySummoningSickness() {
        harness.addToBattlefield(player1, new ElvishArchdruid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
