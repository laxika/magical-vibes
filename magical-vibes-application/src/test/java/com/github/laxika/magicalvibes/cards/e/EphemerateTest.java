package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ephemerate.class, GrizzlyBears.class})
class EphemerateTest extends BaseCardTest {

    @Test
    void flickersCreatureYouControlAndRebounds() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Ephemerate card = new Ephemerate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        var bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearId);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Ephemerate card = new Ephemerate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        var bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileCastSpellTarget.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ephemerate");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void cannotTargetAnOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ephemerate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        var bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void returnsBorrowedCreatureUnderItsOwnersControl() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setOwnerId(player2.getId());
        var original = harness.addToBattlefieldAndReturn(player1, bear);
        gd.stolenCreatures.put(original.getId(), player2.getId());
        harness.setHand(player1, List.of(new Ephemerate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, original.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(original.getId());
    }

    @Test
    void returnsCreatureUntappedWithoutItsOldCounters() {
        var original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Ephemerate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, original.getId());
        harness.passBothPriorities();

        var returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void targetLeavingBattlefieldPreventsRebound() {
        var original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Ephemerate card = new Ephemerate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, original.getId());
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ephemerate");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }
}

@CardUsed({Ephemerate.class, GrizzlyBears.class})
class Mh1EphemerateTest extends BaseCardTest {

    @Test
    void exilesAndImmediatelyReturnsTargetCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Ephemerate card = new Ephemerate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID originalId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, originalId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(originalId);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(cardInExile -> cardInExile.getName().equals("Grizzly Bears"));
    }

    @Test
    void cannotTargetCreatureAnOpponentControls() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ephemerate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reboundOffersFreeCastAtNextUpkeep() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Ephemerate card = new Ephemerate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID originalId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, originalId);
        harness.passBothPriorities();

        UUID firstReturnedId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThat(firstReturnedId).isNotEqualTo(originalId);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstReturnedId);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(firstReturnedId);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Ephemerate");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesTheCardExiled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Ephemerate card = new Ephemerate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Ephemerate");
    }
}
