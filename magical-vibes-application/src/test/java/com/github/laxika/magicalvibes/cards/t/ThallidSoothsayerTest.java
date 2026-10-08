package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SteelLeafChampion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThallidSoothsayer.class, SteelLeafChampion.class, Forest.class})
class ThallidSoothsayerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices chosen creature and puts draw on stack")
    void activatingAbilitySacrificesCreatureAndPutsDrawOnStack() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new SteelLeafChampion());

        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID championId = harness.getPermanentId(player1, "Steel Leaf Champion");
        harness.activateAbility(player1, 0, null, null);
        // Two creatures available — player must choose which to sacrifice
        harness.handlePermanentChosen(player1, championId);

        // Steel Leaf Champion should be sacrificed
        harness.assertNotOnBattlefield(player1, "Steel Leaf Champion");
        harness.assertInGraveyard(player1, "Steel Leaf Champion");

        // Thallid Soothsayer should still be on the battlefield
        harness.assertOnBattlefield(player1, "Thallid Soothsayer");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Thallid Soothsayer");
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Activating ability does NOT tap the Soothsayer")
    void activatingDoesNotTap() {
        Permanent soothsayer = addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new SteelLeafChampion());

        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID championId = harness.getPermanentId(player1, "Steel Leaf Champion");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, championId);

        assertThat(soothsayer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability multiple times in a turn with enough creatures and mana")
    void canActivateMultipleTimes() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new SteelLeafChampion());
        harness.addToBattlefield(player1, new SteelLeafChampion());

        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        // First activation — 3 creatures, must choose
        UUID champion1Id = harness.getPermanentId(player1, "Steel Leaf Champion");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, champion1Id);
        harness.passBothPriorities();

        // Second activation — 2 creatures remain (Soothsayer + Champion), must choose
        UUID champion2Id = harness.getPermanentId(player1, "Steel Leaf Champion");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, champion2Id);
        harness.passBothPriorities();

        // Both Steel Leaf Champions should be sacrificed
        harness.assertNotOnBattlefield(player1, "Steel Leaf Champion");
        // Should have drawn 2 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Resolving ability draws a card")
    void resolvingDrawsACard() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new SteelLeafChampion());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        UUID championId = harness.getPermanentId(player1, "Steel Leaf Champion");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, championId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Soothsayer remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new SteelLeafChampion());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        UUID championId = harness.getPermanentId(player1, "Steel Leaf Champion");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, championId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thallid Soothsayer");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new SteelLeafChampion());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can sacrifice the Soothsayer itself to its own ability")
    void canSacrificeItself() {
        addReadySoothsayer(player1);
        // Soothsayer is the only creature — auto-picks itself
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thallid Soothsayer");
        harness.assertInGraveyard(player1, "Thallid Soothsayer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Soothsayer can activate using generic mana")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent soothsayer = harness.addToBattlefieldAndReturn(player1, new ThallidSoothsayer());
        soothsayer.setSummoningSick(true);
        soothsayer.tap();
        harness.addToBattlefield(player1, new SteelLeafChampion());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Steel Leaf Champion"));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Steel Leaf Champion");
        assertThat(soothsayer.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature or a noncreature permanent")
    void cannotSacrificeOpponentCreatureOrLand() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new SteelLeafChampion());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SteelLeafChampion());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player2, "Steel Leaf Champion")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Forest")))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Steel Leaf Champion");
        harness.assertOnBattlefield(player1, "Forest");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Steel Leaf Champion"));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadySoothsayer(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ThallidSoothsayer());
        perm.setSummoningSick(false);
        return perm;
    }

}
