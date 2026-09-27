package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Naktamun.class, GrizzlyBears.class, LightningBolt.class})
class NaktamunTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Naktamun(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Creature cards in your graveyard have embalm for their mana costs")
    void grantsEmbalmEqualToManaCost() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Naktamun grants embalm only to your creature cards")
    void restrictsEmbalmGrantToOwnCreatureCards() {
        Card instant = new LightningBolt();
        harness.setGraveyard(player1, List.of(instant));
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chaos lets you discard a card to draw a card")
    void chaosMayDiscardToDraw() {
        Card discarded = new LightningBolt();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the chaos ability does nothing")
    void chaosMayBeDeclined() {
        Card inHand = new LightningBolt();
        Card onTop = new GrizzlyBears();
        harness.setHand(player1, List.of(inHand));
        harness.setLibrary(player1, List.of(onTop));

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
