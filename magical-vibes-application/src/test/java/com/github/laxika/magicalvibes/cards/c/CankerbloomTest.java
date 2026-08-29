package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CankerbloomTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode sacrifices Cankerbloom and destroys the target artifact")
    void destroysTargetArtifact() {
        addReadyCankerbloom(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cankerbloom");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Enchantment mode sacrifices Cankerbloom and destroys the target enchantment")
    void destroysTargetEnchantment() {
        addReadyCankerbloom(player1);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cankerbloom");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Proliferate mode sacrifices Cankerbloom and adds a counter")
    void proliferates() {
        addReadyCankerbloom(player1);
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        harness.assertInGraveyard(player1, "Cankerbloom");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Artifact mode cannot target an enchantment")
    void artifactModeRejectsEnchantmentTarget() {
        addReadyCankerbloom(player1);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCankerbloom(Player player) {
        Cankerbloom card = new Cankerbloom();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent permanent = new Permanent(new LeoninScimitar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyEnchantment(Player player) {
        Permanent permanent = new Permanent(new GloriousAnthem());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureWithCounter(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
