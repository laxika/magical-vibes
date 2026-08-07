package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CausticCaterpillarTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices the Caterpillar and destroys target artifact")
    void destroysTargetArtifact() {
        addReadyCaterpillar(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent target = addArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Caustic Caterpillar");
        harness.assertInGraveyard(player1, "Caustic Caterpillar");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysTargetEnchantment() {
        addReadyCaterpillar(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent target = addEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new CausticCaterpillar());
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent target = addEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyCaterpillar(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without mana")
    void cannotActivateWithoutMana() {
        addReadyCaterpillar(player1);
        Permanent target = addEnchantment(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCaterpillar(Player player) {
        Permanent perm = new Permanent(new CausticCaterpillar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEnchantment(Player player) {
        Permanent perm = new Permanent(new GloriousAnthem());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addArtifact(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
