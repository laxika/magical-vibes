package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CityOfShadows;
import com.github.laxika.magicalvibes.cards.f.Fasting;
import com.github.laxika.magicalvibes.cards.f.FellwarStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengerFolk.class, FellwarStone.class, Squire.class, Fasting.class, CityOfShadows.class})
class ScavengerFolkTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Scavenger Folk and destroys target artifact")
    void destroysTargetArtifact() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scavenger Folk");
        harness.assertInGraveyard(player1, "Scavenger Folk");
        harness.assertNotOnBattlefield(player2, "Fellwar Stone");
        harness.assertInGraveyard(player2, "Fellwar Stone");
    }

    @Test
    @DisplayName("Can target own artifact")
    void canTargetOwnArtifact() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fellwar Stone");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutMana() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness (tap cost)")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ScavengerFolk());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent folk = addReadyFolk(player1);
        folk.tap();
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyFolk(player1);
        Permanent creature = addCreatureReady(player2, new Squire());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyFolk(player1);
        Permanent enchantment = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addReadyFolk(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target artifact leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Fellwar Stone"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyFolk(Player player) {
        return addCreatureReady(player, new ScavengerFolk());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FellwarStone());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Fasting());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CityOfShadows());
    }
}
