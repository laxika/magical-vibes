package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BloodRites;
import com.github.laxika.magicalvibes.cards.i.ImiStatue;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamiOfAncientLaw.class, BloodRites.class, WanderingOnes.class, ImiStatue.class})
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
        harness.assertNotOnBattlefield(player2, "Blood Rites");
        harness.assertInGraveyard(player2, "Blood Rites");
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
        Permanent creature = addCreatureReady(player2, new WanderingOnes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        addReadyKami(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ImiStatue());

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
                .removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyKami(Player player) {
        return addCreatureReady(player, new KamiOfAncientLaw());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BloodRites());
    }
}
