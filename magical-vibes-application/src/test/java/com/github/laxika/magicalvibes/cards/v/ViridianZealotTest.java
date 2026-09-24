package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AetherVial;
import com.github.laxika.magicalvibes.cards.s.ScreamsFromWithin;
import com.github.laxika.magicalvibes.cards.t.TelJiladWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViridianZealot.class, AetherVial.class, ScreamsFromWithin.class, TelJiladWolf.class})
class ViridianZealotTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{G}, Sacrifice: destroys target artifact")
    void destroysTargetArtifact() {
        addReadyZealot(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertNotOnBattlefield(player1, "Viridian Zealot");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aether Vial");
        harness.assertInGraveyard(player2, "Aether Vial");
    }

    @Test
    @DisplayName("{1}{G}, Sacrifice: destroys target enchantment")
    void destroysTargetEnchantment() {
        addReadyZealot(player1);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Screams from Within");
        harness.assertInGraveyard(player2, "Screams from Within");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyZealot(player1);
        Permanent creature = addCreatureReady(player2, new TelJiladWolf());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        addReadyZealot(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough generic mana")
    void cannotActivateWithoutEnoughGenericMana() {
        addReadyZealot(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy an artifact it controls")
    void destroysOwnArtifact() {
        addReadyZealot(player1);
        Permanent target = addReadyArtifact(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aether Vial");
        harness.assertInGraveyard(player1, "Aether Vial");
    }

    @Test
    @DisplayName("Can activate despite summoning sickness because it has no tap cost")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ViridianZealot());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Viridian Zealot");
    }

    private Permanent addReadyZealot(Player player) {
        return addCreatureReady(player, new ViridianZealot());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AetherVial());
    }

    private Permanent addReadyEnchantment(Player player) {
        Permanent host = addCreatureReady(player, new TelJiladWolf());
        Permanent aura = harness.addToBattlefieldAndReturn(player, new ScreamsFromWithin());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
