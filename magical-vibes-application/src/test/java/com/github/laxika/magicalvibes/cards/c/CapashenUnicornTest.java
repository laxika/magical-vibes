package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.d.DrakeSkullCameo;
import com.github.laxika.magicalvibes.cards.f.FiresOfYavimaya;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CapashenUnicorn.class, DrakeSkullCameo.class, FiresOfYavimaya.class,
        AlabasterLeech.class, Island.class})
class CapashenUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Capashen Unicorn and destroys target artifact")
    void destroysTargetArtifact() {
        addReadyUnicorn(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Capashen Unicorn");
        harness.assertInGraveyard(player1, "Capashen Unicorn");
        harness.assertNotOnBattlefield(player2, "Drake-Skull Cameo");
        harness.assertInGraveyard(player2, "Drake-Skull Cameo");
    }

    @Test
    @DisplayName("Activating destroys target enchantment")
    void destroysTargetEnchantment() {
        addReadyUnicorn(player1);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fires of Yavimaya");
        harness.assertInGraveyard(player2, "Fires of Yavimaya");
    }

    @Test
    @DisplayName("Cannot activate with only colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        addReadyUnicorn(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new CapashenUnicorn());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyUnicorn(player1);
        Permanent creature = addCreatureReady(player2, new AlabasterLeech());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addReadyUnicorn(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyUnicorn(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Drake-Skull Cameo"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyUnicorn(Player player) {
        return addCreatureReady(player, new CapashenUnicorn());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DrakeSkullCameo());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FiresOfYavimaya());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Island());
    }
}
