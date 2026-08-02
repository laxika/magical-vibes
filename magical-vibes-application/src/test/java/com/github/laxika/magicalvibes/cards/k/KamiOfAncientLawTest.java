package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KamiOfAncientLawTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Kami of Ancient Law and destroys target enchantment")
    void destroysTargetEnchantment() {
        addReadyKami(player1);
        Permanent target = addReadyEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kami of Ancient Law");
        harness.assertInGraveyard(player1, "Kami of Ancient Law");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can activate with summoning sickness because it has no tap cost")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new KamiOfAncientLaw());
        Permanent target = addReadyEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyKami(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        addReadyKami(player1);
        Permanent artifact = new Permanent(new LeoninScimitar());
        gd.playerBattlefields.get(player2.getId()).add(artifact);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyKami(player1);
        Permanent target = addReadyEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Glorious Anthem"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyKami(Player player) {
        Permanent perm = new Permanent(new KamiOfAncientLaw());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyEnchantment(Player player) {
        Permanent perm = new Permanent(new GloriousAnthem());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
