package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IxallisKeeperTest extends BaseCardTest {

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent keeper = addReadyCreature(player1, new IxallisKeeper());
        Permanent target = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability requires {7}{G} mana")
    void requiresMana() {
        Permanent keeper = addReadyCreature(player1, new IxallisKeeper());
        Permanent target = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when summoning sick because ability requires tap")
    void cannotActivateWhenSummoningSick() {
        Permanent keeper = new Permanent(new IxallisKeeper());
        keeper.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(keeper);
        Permanent target = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving ability =====

    @Test
    @DisplayName("Resolving sacrifices Ixalli's Keeper and gives target creature +5/+5 and trample")
    void resolvingSacrificesAndBoostsWithTrample() {
        Permanent keeper = addReadyCreature(player1, new IxallisKeeper());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        // Ixalli's Keeper is sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ixalli's Keeper"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ixalli's Keeper"));

        // Grizzly Bears (2/2) gets +5/+5 = 7/7
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);

        // Grizzly Bears gains trample
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        Permanent keeper = addReadyCreature(player1, new IxallisKeeper());
        Permanent oppBears = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        // Opponent's creature gets +5/+5 and trample
        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, oppBears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at cleanup step")
    void boostAndTrampleWearOffAtCleanup() {
        Permanent keeper = addReadyCreature(player1, new IxallisKeeper());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Helper methods =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
