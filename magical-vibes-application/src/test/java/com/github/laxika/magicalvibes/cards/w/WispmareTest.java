package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.h.HoofprintsOfTheStag;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wispmare.class, HoofprintsOfTheStag.class, HillcomberGiant.class, RayOfCommand.class})
class WispmareTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: ETB destroys target enchantment and Wispmare stays on the battlefield")
    void hardcastDestroysEnchantmentAndStays() {
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hoofprints of the Stag");
        harness.assertInGraveyard(player2, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player1, "Wispmare");
    }

    @Test
    @DisplayName("ETB can target an enchantment controlled by Wispmare's controller")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Hoofprints of the Stag");
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hoofprints of the Stag");
        harness.assertInGraveyard(player1, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player1, "Wispmare");
    }

    @Test
    @DisplayName("Evoke: paying only {W}, ETB still destroys the target enchantment")
    void evokeDestroysEnchantment() {
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Wispmare - sacrifice this creature");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hoofprints of the Stag");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Evoke: Wispmare is sacrificed when its sacrifice trigger resolves")
    void evokeSacrificesSelf() {
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Wispmare - sacrifice this creature");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Wispmare");
        harness.assertInGraveyard(player1, "Wispmare");
    }

    @Test
    @DisplayName("Cannot target a creature with Wispmare's ETB")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new HillcomberGiant());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID creatureId = harness.getPermanentId(player2, "Hillcomber Giant");
        assertThatThrownBy(() ->
                harness.castCreature(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The enchantment trigger resolves before the evoke sacrifice when stacked last")
    void destroysEnchantmentBeforeSacrifice() {
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Wispmare - sacrifice this creature");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player1, "Wispmare");

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Wispmare");
        harness.assertNotOnBattlefield(player1, "Wispmare");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The enchantment trigger still resolves after the evoke sacrifice")
    void destroysEnchantmentAfterSacrifice() {
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2: Wispmare's ETB ability");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wispmare");
        harness.assertNotOnBattlefield(player1, "Wispmare");
        harness.assertOnBattlefield(player2, "Hoofprints of the Stag");

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hoofprints of the Stag");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Wispmare can be hardcast when there are no enchantments")
    void hardcastWithoutEnchantment() {
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Wispmare");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Evoke sacrifices Wispmare even when there are no enchantments")
    void evokeWithoutEnchantment() {
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Wispmare");
        harness.assertInGraveyard(player1, "Wispmare");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Evoke requires white mana")
    void cannotEvokeWithColorlessMana() {
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithEvoke(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Wispmare");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Wispmare.class, HoofprintsOfTheStag.class, RayOfCommand.class})
    @DisplayName("Evoke makes Wispmare's current controller sacrifice it after a control change")
    void currentControllerSacrificesEvokedWispmare() {
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Wispmare - sacrifice this creature");
        UUID wispmareId = harness.getPermanentId(player1, "Wispmare");

        harness.castAndResolveInstant(player2, 0, wispmareId);
        harness.assertOnBattlefield(player2, "Wispmare");

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Wispmare");
        harness.assertNotOnBattlefield(player2, "Wispmare");
        harness.assertInGraveyard(player1, "Wispmare");
        harness.assertInGraveyard(player2, "Hoofprints of the Stag");
        assertThat(gd.stack).isEmpty();
    }
}
