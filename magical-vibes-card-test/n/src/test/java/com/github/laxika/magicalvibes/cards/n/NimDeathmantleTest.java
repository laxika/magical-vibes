package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TelJiladFallen;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimDeathmantle.class, GrizzlyBears.class, Shock.class, TelJiladFallen.class})
class NimDeathmantleTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and intimidate")
    void equippedCreatureGetsBoostAndIntimidate() {
        Permanent deathmantle = harness.addToBattlefieldAndReturn(player1, new NimDeathmantle());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Attach equipment
        deathmantle.setAttachedTo(bears.getId());

        // Verify +2/+2 boost
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4); // 2 + 2

        // Verify intimidate
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature becomes only black and only a Zombie")
    void equippedCreatureGainsColorAndSubtype() {
        Permanent deathmantle = harness.addToBattlefieldAndReturn(player1, new NimDeathmantle());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Attach equipment
        deathmantle.setAttachedTo(bears.getId());

        // Verify replacement of the original color and creature types
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).containsExactly(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Static effects removed when equipment is unequipped")
    void staticEffectsRemovedWhenUnequipped() {
        Permanent deathmantle = harness.addToBattlefieldAndReturn(player1, new NimDeathmantle());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Attach then detach
        deathmantle.setAttachedTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        deathmantle.setAttachedTo(null);

        // Static bonuses should no longer apply
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Death trigger fires when own nontoken creature dies, returns it with equipment attached")
    void deathTriggerReturnsCreatureWithEquipmentAttached() {
        Permanent deathmantle = harness.addToBattlefieldAndReturn(player1, new NimDeathmantle());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Give player1 mana to pay for the trigger
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Kill own creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Resolve Shock, creature dies

        harness.passBothPriorities(); // Resolve the trigger up to its payment choice

        // Should be prompted with may ability to pay {4}
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Accept and pay during resolution
        harness.handleMayAbilityChosen(player1, true);

        // Creature should be back on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");

        // Equipment should be attached to the returned creature
        assertThat(deathmantle.getAttachedTo())
                .isEqualTo(harness.getPermanentId(player1, "Grizzly Bears"));
    }

    @Test
    @DisplayName("Death trigger does not fire for opponent's creatures")
    void deathTriggerDoesNotFireForOpponentCreatures() {
        harness.addToBattlefield(player1, new NimDeathmantle());

        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, opponentBears.getId());
        harness.passBothPriorities(); // Resolve Shock, opponent's creature dies

        // Should NOT be prompted with may ability (opponent's creature, not in our graveyard)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Death trigger allows declining to pay")
    void deathTriggerDeclineToPay() {
        harness.addToBattlefield(player1, new NimDeathmantle());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Kill the creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.passBothPriorities(); // Resolve the trigger up to its payment choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Decline
        harness.handleMayAbilityChosen(player1, false);

        // Creature should stay in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equip pays four mana and applies the bonuses to its target")
    void equipAttachesToControlledCreature() {
        Permanent deathmantle = harness.addToBattlefieldAndReturn(player1, new NimDeathmantle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(deathmantle.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("The return trigger goes on the stack before its optional payment is chosen")
    void paymentChoiceWaitsUntilTriggerResolution() {
        harness.addToBattlefield(player1, new NimDeathmantle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning a creature with protection from artifacts preserves the old attachment")
    void cannotAttachToProtectedReturnedCreature() {
        Permanent deathmantle = harness.addToBattlefieldAndReturn(player1, new NimDeathmantle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        deathmantle.setAttachedTo(bears.getId());
        Permanent fallen = harness.addToBattlefieldAndReturn(player1, new TelJiladFallen());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, fallen.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Tel-Jilad Fallen");
        harness.assertNotInGraveyard(player1, "Tel-Jilad Fallen");
        assertThat(deathmantle.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
    }
}
