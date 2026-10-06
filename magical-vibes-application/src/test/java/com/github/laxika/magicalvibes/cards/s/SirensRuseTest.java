package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.d.DireFleetCaptain;
import com.github.laxika.magicalvibes.cards.f.FathomFleetCaptain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SirensRuse.class, DireFleetCaptain.class, GrizzlyBears.class,
        ArcaneAdaptation.class, FathomFleetCaptain.class, JungleDelver.class})
class SirensRuseTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a non-Pirate creature without drawing a card")
    void flickerNonPirateNoDraw() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, bearsId);

        // Creature should be back on battlefield (new permanent)
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // Should not be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // No card drawn (hand size should not increase — the spell was cast from hand so -1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    @DisplayName("Flickers a Pirate creature and draws a card")
    void flickerPirateDrawsCard() {
        harness.addToBattlefield(player1, new DireFleetCaptain());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID pirateId = harness.getPermanentId(player1, "Dire Fleet Captain");
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, pirateId);

        // Pirate should be back on battlefield
        harness.assertOnBattlefield(player1, "Dire Fleet Captain");
        // Should not be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Dire Fleet Captain"));
        // Drew a card (hand = before - 1 spell + 1 draw = same)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Returned creature has summoning sickness")
    void returnedCreatureHasSummoningSickness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, bearsId);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returned creature goes to its owner (not necessarily the controller)")
    void returnsUnderOwnersControl() {
        // Player1 steals player2's creature, then flickers it — should return to player2 (owner)
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        // Simulate stolen creature: register in stolenCreatures map
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");
        gd.stolenCreatures.put(bearsPermId, player2.getId());

        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, bearsPermId);

        // Should return under player2's control (the owner)
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spell fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);

        // Remove the creature before the spell resolves
        Permanent bearsPerm = gqs.findPermanentById(gd, bearsId);
        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        // Stack should be empty, no crash
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling a Pirate token draws a card even though the token cannot return")
    void drawsForExiledPirateToken() {
        addCreatureReady(player1, new FathomFleetCaptain());
        harness.addToBattlefield(player1, new DireFleetCaptain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        UUID tokenId = harness.getPermanentId(player1, "Pirate");
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.setLibrary(player1, List.of(new JungleDelver()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, tokenId);

        harness.assertNotOnBattlefield(player1, "Pirate");
        harness.assertInHand(player1, "Jungle Delver");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A creature granted the Pirate type by Arcane Adaptation draws a card")
    void drawsForGrantedPirateSubtype() {
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.PIRATE);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.setLibrary(player1, List.of(new DireFleetCaptain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Jungle Delver");
        harness.assertInHand(player1, "Dire Fleet Captain");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A stolen Pirate returns to its owner but the caster draws the card")
    void stolenPirateDrawsForCaster() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new DireFleetCaptain());
        gd.stolenCreatures.put(pirate.getId(), player2.getId());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new JungleDelver()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, pirate.getId());

        harness.assertOnBattlefield(player2, "Dire Fleet Captain");
        harness.assertNotOnBattlefield(player1, "Dire Fleet Captain");
        harness.assertInHand(player1, "Jungle Delver");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flickering returns an untapped new creature without its old counters")
    void returnsNewUntappedCreatureWithoutCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        creature.tap();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        Permanent returned = findPermanent(player1, "Jungle Delver");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An own noncreature permanent is not a legal target")
    void cannotTargetNoncreature() {
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.PIRATE);
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, adaptation.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
