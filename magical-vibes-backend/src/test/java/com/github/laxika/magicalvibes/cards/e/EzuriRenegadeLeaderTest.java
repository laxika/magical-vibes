package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EzuriRenegadeLeaderTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has regenerate ability and overrun ability")
    void hasCorrectAbilities() {
        EzuriRenegadeLeader card = new EzuriRenegadeLeader();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 0: {G}: Regenerate another target Elf
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{G}");
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();

        // Ability 1: {2}{G}{G}{G}: Elf overrun
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{2}{G}{G}{G}");
        assertThat(card.getActivatedAbilities().get(1).getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().get(1).getEffects().get(0))
                .isInstanceOf(BoostAllOwnCreaturesEffect.class);
        assertThat(card.getActivatedAbilities().get(1).getEffects().get(1))
                .isInstanceOf(GrantKeywordEffect.class);
    }

    // ===== Regenerate ability =====

    @Test
    @DisplayName("Regenerate ability targets another Elf and grants regeneration shield")
    void regenerateTargetsAnotherElf() {
        Permanent ezuri = addReadyCreature(player1, new EzuriRenegadeLeader());
        Permanent elf = addReadyCreature(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, elf.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(elf.getId());

        harness.passBothPriorities();

        assertThat(elf.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerate ability cannot target Ezuri itself")
    void cannotRegenerateItself() {
        Permanent ezuri = addReadyCreature(player1, new EzuriRenegadeLeader());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ezuri.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regenerate ability cannot target a non-Elf creature")
    void cannotRegenerateNonElf() {
        Permanent ezuri = addReadyCreature(player1, new EzuriRenegadeLeader());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regenerate ability can target an opponent's Elf")
    void canRegenerateOpponentElf() {
        Permanent ezuri = addReadyCreature(player1, new EzuriRenegadeLeader());
        Permanent opponentElf = addReadyCreature(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentElf.getId());
        harness.passBothPriorities();

        assertThat(opponentElf.getRegenerationShield()).isEqualTo(1);
    }

    // ===== Overrun ability =====

    @Test
    @DisplayName("Overrun gives +3/+3 and trample to Elf creatures you control")
    void overrunBoostsElves() {
        Permanent ezuri = addReadyCreature(player1, new EzuriRenegadeLeader());
        Permanent elf = addReadyCreature(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Ezuri (2/2 Elf) gets +3/+3 = 5/5
        assertThat(gqs.getEffectivePower(gd, ezuri)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ezuri)).isEqualTo(5);
        assertThat(ezuri.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        // Llanowar Elves (1/1 Elf) gets +3/+3 = 4/4
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);
        assertThat(elf.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Overrun does not affect non-Elf creatures")
    void overrunDoesNotAffectNonElves() {
        Permanent ezuri = addReadyCreature(player1, new EzuriRenegadeLeader());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Grizzly Bears (2/2 non-Elf) should NOT be boosted
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Overrun does not affect opponent's Elf creatures")
    void overrunDoesNotAffectOpponentElves() {
        Permanent ezuri = addReadyCreature(player1, new EzuriRenegadeLeader());
        Permanent opponentElf = addReadyCreature(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Opponent's Llanowar Elves (1/1) should NOT be boosted
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
        assertThat(opponentElf.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Overrun requires 5 mana (2GGG)")
    void overrunRequiresEnoughMana() {
        Permanent ezuri = addReadyCreature(player1, new EzuriRenegadeLeader());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Helper methods =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
