package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OsteomancerAdept.class, BakersbaneDuo.class})
class OsteomancerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a creature from the graveyard by exiling three cards and gives it finality")
    void castsCreatureByForagingFromGraveyard() {
        Permanent adept = addReadyAdept();
        Card creature = new OsteomancerAdept();
        harness.setGraveyard(player1, List.of(new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept(), creature));
        grantPermission(adept);
        addCreatureMana();

        harness.castFromGraveyard(player1, 3, List.of(0, 1, 2));
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, entered));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Casts a creature from the graveyard by sacrificing a Food")
    void castsCreatureBySacrificingFood() {
        Permanent adept = addReadyAdept();
        Permanent food = addFoodToken();
        Card creature = new OsteomancerAdept();
        harness.setGraveyard(player1, List.of(creature));
        grantPermission(adept);
        addCreatureMana();

        harness.getGameService().playFlashbackSpell(
                gd, player1, 0, null, null, List.of(), null, null, List.of(), null, food.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent == food);
        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot replace mandatory foraging with mana")
    void cannotPayManaInsteadOfForaging() {
        Permanent adept = addReadyAdept();
        Card creature = new OsteomancerAdept();
        harness.setGraveyard(player1, List.of(creature));
        grantPermission(adept);
        addCreatureMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("forage");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void cannotCastFromOpponentsGraveyard() {
        Permanent adept = addReadyAdept();
        Card creature = new OsteomancerAdept();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(
                new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept()));
        grantPermission(adept);
        addCreatureMana();

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, creature.getId(),
                null, null, List.of(), List.of(0, 1, 2), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    void permissionAllowsMultipleCreaturesIncludingCardsAddedLater() {
        Permanent adept = addReadyAdept();
        grantPermission(adept);
        for (int cast = 0; cast < 2; cast++) {
            Card creature = new OsteomancerAdept();
            harness.setGraveyard(player1, List.of(
                    new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept(), creature));
            addCreatureMana();
            harness.castFromGraveyard(player1, 3, List.of(0, 1, 2));
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anySatisfy(permanent -> {
                        assertThat(permanent.getCard().getId()).isEqualTo(creature.getId());
                        assertThat(permanent.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
                    });
        }
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(6);
    }

    @Test
    void permissionSurvivesAdeptLeavingBattlefield() {
        Permanent adept = addReadyAdept();
        grantPermission(adept);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, adept));
        harness.setGraveyard(player1, List.of(
                new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept(), adept.getCard()));
        addCreatureMana();
        harness.castFromGraveyard(player1, 3, List.of(0, 1, 2));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(adept.getCard().getId());
            assertThat(permanent.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        });
    }

    @Test
    void foragingDoesNotReplaceManaCost() {
        Permanent adept = addReadyAdept();
        Card creature = new OsteomancerAdept();
        harness.setGraveyard(player1, List.of(
                new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept(), creature));
        grantPermission(adept);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 3, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotUseSpellItselfAsOneOfThreeForageCards() {
        Permanent adept = addReadyAdept();
        harness.setGraveyard(player1, List.of(
                new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept()));
        grantPermission(adept);
        addCreatureMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 2, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void creatureCastingStillRequiresNormalTiming() {
        Permanent adept = addReadyAdept();
        harness.setGraveyard(player1, List.of(
                new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept()));
        grantPermission(adept);
        harness.forceStep(TurnStep.UPKEEP);
        addCreatureMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 3, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    void permissionExpiresAtEndOfTurn() {
        Permanent adept = addReadyAdept();
        grantPermission(adept);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(
                new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept()));
        addCreatureMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 3, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    void finalityDoesNotExileCreatureReturningToHand() {
        Permanent adept = addReadyAdept();
        Card creature = new OsteomancerAdept();
        harness.setGraveyard(player1, List.of(
                new OsteomancerAdept(), new OsteomancerAdept(), new OsteomancerAdept(), creature));
        grantPermission(adept);
        addCreatureMana();
        harness.castFromGraveyard(player1, 3, List.of(0, 1, 2));
        harness.passBothPriorities();
        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, entered));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    private Permanent addReadyAdept() {
        return addCreatureReady(player1, new OsteomancerAdept());
    }

    private void grantPermission(Permanent adept) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(adept);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }

    private void addCreatureMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent addFoodToken() {
        harness.enterBattlefieldAndReturn(player1, new BakersbaneDuo());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.FOOD))
                .findFirst()
                .orElseThrow();
    }
}
