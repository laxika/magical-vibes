package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AzoriusArrester;
import com.github.laxika.magicalvibes.cards.c.ChromaticLantern;
import com.github.laxika.magicalvibes.cards.m.MartialLaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeningApparition.class, MartialLaw.class, AzoriusArrester.class, ChromaticLantern.class})
class KeeningApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Keening Apparition and destroys target enchantment")
    void destroysTargetEnchantment() {
        addReadyApparition(player1);
        Permanent target = addReadyEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Keening Apparition");
        harness.assertInGraveyard(player1, "Keening Apparition");
        harness.assertNotOnBattlefield(player2, "Martial Law");
        harness.assertInGraveyard(player2, "Martial Law");
    }

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new KeeningApparition());
        Permanent target = addReadyEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyApparition(player1);
        Permanent creature = addCreatureReady(player2, new AzoriusArrester());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        addReadyApparition(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticLantern());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyApparition(player1);
        Permanent target = addReadyEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Sacrifice is paid before the enchantment is destroyed")
    void sacrificesAsCostBeforeResolution() {
        addReadyApparition(player1);
        Permanent target = addReadyEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Keening Apparition");
        harness.assertInGraveyard(player1, "Keening Apparition");
        harness.assertOnBattlefield(player2, "Martial Law");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Martial Law");
    }

    @Test
    @DisplayName("A tapped apparition can destroy its controller's enchantment")
    void tappedApparitionCanDestroyOwnEnchantment() {
        Permanent apparition = harness.addToBattlefieldAndReturn(player1, new KeeningApparition());
        apparition.tap();
        Permanent target = addReadyEnchantment(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Keening Apparition");
        harness.assertNotOnBattlefield(player1, "Martial Law");
        harness.assertInGraveyard(player1, "Martial Law");
    }

    private void addReadyApparition(Player player) {
        addCreatureReady(player, new KeeningApparition());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MartialLaw());
    }
}
