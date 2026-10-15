package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrindleBoar.class})
class BrindleBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Brindle Boar puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BrindleBoar()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BrindleBoar.class);
    }

    @Test
    @DisplayName("Resolving Brindle Boar puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new BrindleBoar()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brindle Boar");
    }

    @Test
    @DisplayName("Sacrificing Brindle Boar puts ability on stack and moves it to graveyard")
    void sacrificingPutsAbilityOnStackAndMovesToGraveyard() {
        addBrindleBoarReady(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Brindle Boar should be sacrificed (moved to graveyard as a cost)
        harness.assertNotOnBattlefield(player1, "Brindle Boar");
        harness.assertInGraveyard(player1, "Brindle Boar");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BrindleBoar.class);
    }

    @Test
    @DisplayName("Resolving the ability gains 4 life")
    void resolvingAbilityGains4Life() {
        addBrindleBoarReady(player1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
    }

    @Test
    @DisplayName("Ability has no mana cost — can activate without mana")
    void canActivateWithoutMana() {
        addBrindleBoarReady(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTap() {
        Permanent boar = addBrindleBoarReady(player1);
        boar.tap();

        // Should still be able to activate even when tapped
        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Summoning sickness does not prevent sacrificing Brindle Boar")
    void canActivateWhileSummoningSick() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new BrindleBoar());
        boar.setSummoningSick(true);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Brindle Boar");
        harness.assertInGraveyard(player1, "Brindle Boar");
        harness.assertLife(player1, startingLife);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the activating player gains life")
    void secondPlayerGainsLifeOnResolution() {
        harness.addToBattlefield(player2, new BrindleBoar());
        int firstPlayerLife = gd.playerLifeTotals.get(player1.getId());
        int secondPlayerLife = gd.playerLifeTotals.get(player2.getId());
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.assertLife(player2, secondPlayerLife);
        harness.assertInGraveyard(player2, "Brindle Boar");
        harness.passBothPriorities();

        harness.assertLife(player2, secondPlayerLife + 4);
        harness.assertLife(player1, firstPlayerLife);
    }

    private Permanent addBrindleBoarReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BrindleBoar());
        perm.setSummoningSick(false);
        return perm;
    }
}
