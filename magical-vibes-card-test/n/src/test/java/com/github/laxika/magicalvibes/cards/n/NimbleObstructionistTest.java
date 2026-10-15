package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CunningSurvivor;
import com.github.laxika.magicalvibes.cards.d.DesertOfTheMindful;
import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NimbleObstructionist.class, FumeSpitter.class, GrizzlyBears.class,
        SerraAngel.class, Shock.class, CunningSurvivor.class, DesertOfTheMindful.class})
class NimbleObstructionistTest extends BaseCardTest {

    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
    }

    @Test
    @DisplayName("Cycling counters an opponent's activated ability and still draws")
    void cyclingCountersOpponentActivatedAbilityAndDraws() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);

        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        // Player2 activates Fume Spitter's ability targeting player1's Grizzly Bears.
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        // Player1 cycles Nimble Obstructionist, countering Fume Spitter's ability.
        harness.activateHandAbility(player1, 0, fumeSpitter.getId());
        harness.passBothPriorities();

        // Ability countered: Grizzly Bears never received the -1/-1 counter and survives.
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isZero();

        // The trigger resolves before the separate cycling draw.
        harness.assertNotInHand(player1, "Serra Angel");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nimble Obstructionist");
        harness.assertInHand(player1, "Serra Angel");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling with no legal target still draws a card")
    void cyclingWithoutTargetStillDraws() {
        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nimble Obstructionist");
        harness.assertInHand(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Cannot target an activated ability you control")
    void cannotCounterOwnAbility() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);

        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player1, fumeSpitter);

        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        // Player1 activates their own Fume Spitter targeting player2's Grizzly Bears.
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        // Player1 cannot cycle to counter their own ability.
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, fumeSpitter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotCounterSpell() {
        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        // Player2 casts Shock targeting player1.
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        // Nimble Obstructionist can only counter abilities, not spells.
        UUID shockId = shock.getId();
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, shockId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling counters an opposing triggered ability before the draw")
    void countersOpponentTriggeredAbility() {
        var survivor = harness.addToBattlefieldAndReturn(player2, new CunningSurvivor());
        harness.setHand(player2, List.of(new DesertOfTheMindful()));
        harness.setLibrary(player2, List.of(new NimbleObstructionist()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateHandAbility(player2, 0, null);
        UUID triggerId = gd.stack.getLast().getTargetableId();

        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new DesertOfTheMindful()));
        addCyclingMana(player1);
        harness.activateHandAbility(player1, 0, triggerId);
        harness.passBothPriorities();

        assertThat(survivor.getPowerModifier()).isZero();
        assertThat(survivor.isCantBeBlocked()).isFalse();
        harness.assertNotInHand(player1, "Desert of the Mindful");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Desert of the Mindful");
        harness.passBothPriorities();
        harness.assertInHand(player2, "Nimble Obstructionist");
    }

    @Test
    @DisplayName("Countering the cycling trigger leaves its cycling draw intact")
    void counteredTriggerStillAllowsCyclingDraw() {
        harness.setHand(player2, List.of(new DesertOfTheMindful(), new NimbleObstructionist()));
        harness.setLibrary(player2, List.of(new CunningSurvivor(), new CunningSurvivor()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateHandAbility(player2, 0, null);
        UUID desertAbilityId = gd.stack.getLast().getTargetableId();

        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new DesertOfTheMindful()));
        addCyclingMana(player1);
        harness.activateHandAbility(player1, 0, desertAbilityId);
        UUID counterTriggerId = gd.stack.getLast().getTargetableId();
        harness.activateHandAbility(player2, 0, counterTriggerId);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Desert of the Mindful");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An illegal counter-trigger target does not prevent the cycling draw")
    void illegalTriggerTargetStillAllowsCyclingDraw() {
        harness.setHand(player2, List.of(new DesertOfTheMindful()));
        harness.setLibrary(player2, List.of(new CunningSurvivor()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateHandAbility(player2, 0, null);
        UUID desertAbilityId = gd.stack.getLast().getTargetableId();

        harness.setHand(player1, List.of(new NimbleObstructionist(), new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new DesertOfTheMindful(), new DesertOfTheMindful()));
        addCyclingMana(player1);
        addCyclingMana(player1);
        harness.activateHandAbility(player1, 0, desertAbilityId);
        harness.activateHandAbility(player1, 0, desertAbilityId);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player2, "Cunning Survivor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A legal opposing ability must be targeted by the cycling trigger")
    void cannotDeclineMandatoryCounterTrigger() {
        harness.setHand(player2, List.of(new DesertOfTheMindful()));
        harness.setLibrary(player2, List.of(new CunningSurvivor()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateHandAbility(player2, 0, null);

        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new DesertOfTheMindful()));
        addCyclingMana(player1);
        harness.activateHandAbility(player1, 0, null);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, gd.stack.getFirst().getTargetableId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Desert of the Mindful");
        harness.assertNotInHand(player2, "Cunning Survivor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows casting on the opponent's turn with a spell on the stack")
    void canCastWithFlashInResponseToOpponentSpell() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new NimbleObstructionist(), "{2}{U}");
        harness.castFromHand(player1, new NimbleObstructionist(), "{2}{U}");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nimble Obstructionist");
        harness.assertNotOnBattlefield(player2, "Nimble Obstructionist");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Nimble Obstructionist");
    }

    @Test
    @DisplayName("Cycling requires the full cost and discards before drawing")
    void cyclingPaysFullCostAndDiscardsImmediately() {
        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new DesertOfTheMindful()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Nimble Obstructionist");
        harness.assertNotInGraveyard(player1, "Nimble Obstructionist");
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Nimble Obstructionist");
        harness.assertNotInHand(player1, "Nimble Obstructionist");
        harness.assertNotInHand(player1, "Desert of the Mindful");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Desert of the Mindful");
    }
}
