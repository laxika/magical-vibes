package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.d.DawningAngel;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodForBones.class, GrizzlyBears.class, LlanowarElves.class, Shock.class,
        GreenwoodSentinel.class, DawningAngel.class})
class BloodForBonesTest extends BaseCardTest {

    @Test
    void sacrificesACreatureAndReturnsAnotherCreatureToBattlefieldAndHand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Card battlefieldCard = new GrizzlyBears();
        Card handCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(battlefieldCard, handCard, new Shock()));
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();

        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(battlefieldCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(handCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(battlefieldCard.getId())
                        || card.getId().equals(handCard.getId()));
    }

    @Test
    void cannotCastWithoutAcreatureToSacrifice() {
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void canReturnTheSacrificedCreatureWithNoCreaturesInitiallyInGraveyard() {
        Card creature = new GreenwoodSentinel();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId())
                        && !permanent.isTapped() && permanent.isSummoningSick());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canReturnTheSacrificedCreatureToHandAfterReturningAnotherToBattlefield() {
        Card sacrificedCard = new GreenwoodSentinel();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, sacrificedCard);
        Card returnedCard = new GreenwoodSentinel();
        harness.setGraveyard(player1, List.of(returnedCard, new Shock()));
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnedCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sacrificedCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotDeclineReturningACreatureToBattlefield() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot decline");
        harness.handleGraveyardCardChosen(player1, 0);
    }

    @Test
    void cannotDeclineReturningAnotherCreatureToHand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot decline");
        harness.handleGraveyardCardChosen(player1, 0);
    }

    @Test
    void enterBattlefieldTriggerWaitsUntilBothReturnsAreFinished() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Card angel = new DawningAngel();
        harness.setGraveyard(player1, List.of(angel));
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sacrifice.getCard());
        harness.assertLife(player1, lifeBefore);

        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(angel.getId()));
    }
}
