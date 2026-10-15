package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViciousShadows.class, GrizzlyBears.class, Shock.class, Naturalize.class,
        PlanarCleansing.class, Pyroclasm.class, CircleOfProtectionRed.class})
class ViciousShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to target player equal to that player's hand size when accepting")
    void dealsDamageEqualToHandSize() {
        harness.addToBattlefield(player1, new ViciousShadows());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        // Target player2 will hold three cards in hand at resolution.
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        // player1 kills the creature with Shock, triggering "whenever a creature dies".
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new ViciousShadows());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Targeting a player with an empty hand deals no damage")
    void emptyHandDealsNoDamage() {
        harness.addToBattlefield(player1, new ViciousShadows());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Uses the target player's hand size at resolution after a response")
    void countsHandAtResolution() {
        harness.addToBattlefield(player1, new ViciousShadows());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An allied creature's death can deal damage to the enchantment's controller")
    void alliedDeathCanTargetController() {
        harness.addToBattlefield(player1, new ViciousShadows());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Each creature dying simultaneously produces a separate optional damage trigger")
    void simultaneousDeathsTriggerSeparately() {
        harness.addToBattlefield(player1, new ViciousShadows());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The damage trigger still resolves if the enchantment is destroyed in response")
    void triggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new ViciousShadows());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Naturalize(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Vicious Shadows"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Vicious Shadows");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Triggers for creature deaths even when the enchantment is destroyed simultaneously")
    void triggersWhenSourceAndCreatureAreDestroyedTogether() {
        harness.addToBattlefield(player1, new ViciousShadows());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Vicious Shadows");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevention naming Vicious Shadows as the source prevents its triggered damage")
    void sourceSpecificPreventionStopsDamage() {
        harness.addToBattlefield(player1, new ViciousShadows());
        harness.addToBattlefield(player2, new CircleOfProtectionRed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, harness.getPermanentId(player1, "Vicious Shadows"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 20);
    }
}
