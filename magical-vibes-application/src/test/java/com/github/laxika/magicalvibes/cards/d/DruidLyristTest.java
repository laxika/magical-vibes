package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AegisOfHonor;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.c.CatalystStone;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DruidLyrist.class, AegisOfHonor.class, AngelicWall.class, CatalystStone.class, Island.class})
class DruidLyristTest extends BaseCardTest {

    @Test
    @DisplayName("Pays the activation costs when the ability is activated")
    void paysActivationCosts() {
        addCreatureReady(player1, new DruidLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AegisOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Druid Lyrist");
        harness.assertInGraveyard(player1, "Druid Lyrist");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Activating sacrifices Druid Lyrist and destroys target enchantment")
    void destroysTargetEnchantment() {
        addCreatureReady(player1, new DruidLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AegisOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Druid Lyrist");
        harness.assertInGraveyard(player1, "Druid Lyrist");
        harness.assertNotOnBattlefield(player2, "Aegis of Honor");
        harness.assertInGraveyard(player2, "Aegis of Honor");
    }

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        addCreatureReady(player1, new DruidLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AegisOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aegis of Honor");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new DruidLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AegisOfHonor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness (tap cost)")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new DruidLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AegisOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new DruidLyrist());
        Permanent creature = addCreatureReady(player2, new AngelicWall());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        addCreatureReady(player1, new DruidLyrist());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CatalystStone());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new DruidLyrist());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target enchantment leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new DruidLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AegisOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Aegis of Honor"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

}
