package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.q.QuirionElves;
import com.github.laxika.magicalvibes.cards.d.DismantlingBlow;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
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

@CardUsed({Bind.class, RodOfRuin.class, Shock.class, QuirionElves.class,
        DismantlingBlow.class, RavenousRats.class, ShivanDragon.class})
class BindTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an activated ability and draws a card")
    void countersActivatedAbilityAndDraws() {
        Bind bind = new Bind();
        harness.setHand(player1, List.of(bind));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, rod.getId());

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotTargetSpell() {
        harness.setHand(player1, List.of(new Bind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        UUID shockId = shock.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, shockId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a mana ability")
    void cannotTargetManaAbility() {
        Permanent elves = addCreatureReady(player2, new QuirionElves());
        harness.setHand(player1, List.of(new Bind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        harness.passPriority(player2);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, elves.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a triggered ability")
    void cannotTargetTriggeredAbility() {
        harness.setHand(player1, List.of(new Bind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new RavenousRats()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        UUID triggerId = gd.stack.getFirst().getTargetableId();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, triggerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not draw when the targeted ability has already been countered")
    void doesNotDrawWithMissingTarget() {
        harness.setHand(player1, List.of(new Bind(), new Bind()));
        Bind drawnCard = new Bind();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, rod.getId());
        harness.castAndResolveInstant(player1, 0, rod.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can counter an ability after its source has been destroyed")
    void countersAbilityAfterSourceLeaves() {
        harness.setHand(player1, List.of(new DismantlingBlow(), new Bind()));
        Bind drawnCard = new Bind();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        var activationId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, rod.getId());
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, activationId);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counters the selected activation when one source has multiple abilities on the stack")
    void countersSelectedActivationFromSameSource() {
        harness.setHand(player1, List.of(new Bind()));
        harness.setLibrary(player1, List.of(new Bind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new ShivanDragon());
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        var firstActivation = gd.stack.getFirst();
        harness.activateAbility(player2, 0, null, null);
        var secondActivation = gd.stack.getLast();
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, secondActivation.getTargetableId());

        assertThat(gd.stack).containsExactly(firstActivation);
    }
}
