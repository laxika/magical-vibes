package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DomriRade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruulGuildmage.class, GruulGuildgate.class, DomriRade.class})
class GruulGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land deals 2 damage to a target player")
    void sacrificesLandAndDamagesPlayer() {
        addReadyGuildmage(player1);
        harness.addToBattlefield(player1, new GruulGuildgate());
        int lifeBefore = gd.getLife(player2.getId());

        addRedAbilityMana();
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Gruul Guildgate");
    }

    @Test
    @DisplayName("The first ability can target a planeswalker")
    void damagesTargetPlaneswalker() {
        addReadyGuildmage(player1);
        harness.addToBattlefield(player1, new GruulGuildgate());

        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DomriRade());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        addRedAbilityMana();
        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Gruul Guildgate");
    }

    @Test
    @DisplayName("The second ability gives a target creature +2/+2 until end of turn")
    void boostsTargetCreatureUntilEndOfTurn() {
        Permanent guildmage = addReadyGuildmage(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulGuildmage());
        int powerBefore = gqs.getEffectivePower(gd, target);
        int toughnessBefore = gqs.getEffectiveToughness(gd, target);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guildmage), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughnessBefore + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("The first ability cannot target a creature")
    void cannotTargetCreatureWithDamageAbility() {
        addReadyGuildmage(player1);
        harness.addToBattlefield(player1, new GruulGuildgate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulGuildmage());

        addRedAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The first ability cannot be activated without a land to sacrifice")
    void cannotActivateWithoutLand() {
        addReadyGuildmage(player1);
        addRedAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }

    @Test
    @DisplayName("A tapped land is sacrificed before damage resolves, and the controller can be targeted")
    void paysSacrificeBeforeResolvingDamageToController() {
        addReadyGuildmage(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GruulGuildgate());
        land.tap();
        int lifeBefore = gd.getLife(player1.getId());

        addRedAbilityMana();
        harness.activateAbility(player1, 0, 0, null, player1.getId());

        harness.assertInGraveyard(player1, "Gruul Guildgate");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("An opponent's land cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsLand() {
        addReadyGuildmage(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GruulGuildgate());
        addRedAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("The damage ability requires red mana even when generic mana is available")
    void cannotPayRedCostWithOnlyGreenMana() {
        addReadyGuildmage(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GruulGuildgate());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Guildmage can activate its pump repeatedly on itself")
    void pumpCanBeRepeatedWhileTappedAndSummoningSick() {
        Permanent guildmage = addReadyGuildmage(player1);
        guildmage.tap();
        guildmage.setSummoningSick(true);
        int powerBefore = gqs.getEffectivePower(gd, guildmage);
        int toughnessBefore = gqs.getEffectiveToughness(gd, guildmage);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, guildmage.getId());
        harness.activateAbility(player1, 0, 1, null, guildmage.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(powerBefore + 4);
        assertThat(gqs.getEffectiveToughness(gd, guildmage)).isEqualTo(toughnessBefore + 4);
        assertThat(guildmage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The pump ability cannot target a land")
    void pumpCannotTargetLand() {
        addReadyGuildmage(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GruulGuildgate());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGuildmage(Player player) {
        Permanent perm = addCreatureReady(player, new GruulGuildmage());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void addRedAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
