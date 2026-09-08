package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SokenzanSpellbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido 1 triggers when Sokenzan Spellblade becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent spellblade = addReadySpellblade(player1);
        spellblade.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spellblade.getPowerModifier()).isEqualTo(1);
        assertThat(spellblade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 triggers when Sokenzan Spellblade blocks")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent spellblade = addReadySpellblade(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spellblade.getPowerModifier()).isEqualTo(1);
        assertThat(spellblade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability uses the controller's hand size when it resolves")
    void activatedAbilityUsesHandSizeAtResolution() {
        Permanent spellblade = addReadySpellblade(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(spellblade.getPowerModifier()).isEqualTo(3);
        assertThat(spellblade.getToughnessModifier()).isZero();
    }

    private Permanent addReadySpellblade(Player player) {
        Permanent permanent = new Permanent(new SokenzanSpellblade());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
