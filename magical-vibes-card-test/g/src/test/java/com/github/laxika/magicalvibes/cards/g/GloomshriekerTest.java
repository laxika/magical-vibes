package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TamiyosCompleation;
import com.github.laxika.magicalvibes.cards.u.UnchartedHaven;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gloomshrieker.class, GrizzlyBears.class, Shock.class, TamiyosCompleation.class,
        UnchartedHaven.class})
class GloomshriekerTest extends BaseCardTest {

    @Test
    void entersAndReturnsTargetPermanentCardFromGraveyardToHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castGloomshrieker();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotTargetNonPermanentCardsInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castGloomshrieker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void isExiledInsteadOfDying() {
        var gloomshrieker = harness.addToBattlefieldAndReturn(player1, new Gloomshrieker());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, gloomshrieker.getId());

        harness.assertNotOnBattlefield(player1, "Gloomshrieker");
        harness.assertNotInGraveyard(player1, "Gloomshrieker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gloomshrieker"));
    }

    @Test
    void returnsALandCardFromGraveyard() {
        UnchartedHaven land = new UnchartedHaven();
        harness.setGraveyard(player1, List.of(land));
        castGloomshrieker();

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        harness.assertNotInGraveyard(player1, "Uncharted Haven");
    }

    @Test
    void doesNotTargetAnOpponentsPermanentCard() {
        harness.setGraveyard(player2, List.of(new Gloomshrieker()));
        castGloomshrieker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Gloomshrieker");
        harness.assertNotInHand(player1, "Gloomshrieker");
    }

    @Test
    void doesNotReturnAnotherCardWhenTheTargetLeavesTheGraveyard() {
        UnchartedHaven target = new UnchartedHaven();
        Gloomshrieker other = new Gloomshrieker();
        harness.setGraveyard(player1, List.of(target, other));
        castGloomshrieker();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target, other);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
    }

    @Test
    void returnTriggerStillResolvesAfterGloomshriekerLeaves() {
        UnchartedHaven target = new UnchartedHaven();
        harness.setGraveyard(player1, List.of(target));
        castGloomshrieker();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Gloomshrieker"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gloomshrieker");
        assertThat(gd.playerHands.get(player1.getId())).contains(target);
    }

    @Test
    void goesToGraveyardWhenItsAbilitiesHaveBeenRemoved() {
        var gloomshrieker = harness.addToBattlefieldAndReturn(player1, new Gloomshrieker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, gloomshrieker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, gloomshrieker.getId());

        harness.assertNotOnBattlefield(player1, "Gloomshrieker");
        harness.assertInGraveyard(player1, "Gloomshrieker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Gloomshrieker"));
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new Gloomshrieker());
        addCreatureReady(player2, new Gloomshrieker());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new Gloomshrieker());
        var first = addCreatureReady(player2, new Gloomshrieker());
        var second = addCreatureReady(player2, new Gloomshrieker());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private void castGloomshrieker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Gloomshrieker(), "{1}{B}{G}");
        harness.passBothPriorities();
    }
}
