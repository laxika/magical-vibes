package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulOfWindgrace.class, Forest.class, GrizzlyBears.class})
class SoulOfWindgraceTest extends BaseCardTest {

    @Test
    void etbMayReturnsTappedLandFromEitherGraveyard() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(forest));
        castSoul();

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .anyMatch(card -> card.getName().equals("Forest"))).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        Permanent returnedForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest"))
                .findFirst()
                .orElseThrow();
        assertThat(returnedForest.isTapped()).isTrue();
    }

    @Test
    void greenAbilityDiscardsOnlyLandAndGainsLife() {
        addReadySoul(player1);
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }

    @Test
    void redAbilityDiscardsLandAndDrawsCard() {
        addReadySoul(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void blackAbilityTapsSoulAndGrantsIndestructibleUntilEndOfTurn() {
        Permanent soul = addReadySoul(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(soul.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, soul, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soul, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void attackReturnsLandFromControllersGraveyardTapped() {
        addReadySoul(player1);
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
    }

    @Test
    void mayDeclineReturningLand() {
        harness.setGraveyard(player2, List.of(new Forest()));
        castSoul();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    @Test
    void nonlandCardsCannotBeReturned() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        castSoul();
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void discardIsPaidBeforeLifeGainResolves() {
        addReadySoul(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }

    @Test
    void blackAbilityCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent soul = harness.addToBattlefieldAndReturn(player1, new SoulOfWindgrace());
        soul.setSummoningSick(true);
        soul.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gqs.hasKeyword(gd, soul, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();

        assertThat(soul.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, soul, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Forest");
    }

    private void castSoul() {
        harness.setHand(player1, List.of(new SoulOfWindgrace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadySoul(Player player) {
        Permanent soul = harness.addToBattlefieldAndReturn(player, new SoulOfWindgrace());
        soul.setSummoningSick(false);
        return soul;
    }
}
