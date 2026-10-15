package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RalCracklingWit;
import com.github.laxika.magicalvibes.cards.g.GoblinElectromancer;
import com.github.laxika.magicalvibes.cards.s.SeasonedWarrenguard;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeedTheCycle.class, RalCracklingWit.class, SeasonedWarrenguard.class, Plains.class})
class FeedTheCycleTest extends BaseCardTest {

    @Test
    @DisplayName("Forages by exiling three graveyard cards and destroys a creature")
    void foragesByExilingGraveyardCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeasonedWarrenguard());
        List<Card> graveyard = List.of(new SeasonedWarrenguard(), new SeasonedWarrenguard(), new SeasonedWarrenguard());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new FeedTheCycle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1, 2));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        harness.assertOnBattlefield(player2, "Seasoned Warrenguard");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Forages by sacrificing a Food and destroys a planeswalker")
    void foragesBySacrificingFood() {
        Permanent food = addFoodToken(player1);
        Permanent target = addReadyPlaneswalker(player2, 3);
        harness.setHand(player1, List.of(new FeedTheCycle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), food.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(food.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(food.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getName().equals("Food"));
    }

    @Test
    @DisplayName("Pays {B} instead of foraging")
    void paysManaInsteadOfForaging() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeasonedWarrenguard());
        harness.setHand(player1, List.of(new FeedTheCycle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Seasoned Warrenguard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without foraging or paying the additional mana")
    void requiresForageOrMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeasonedWarrenguard());
        harness.setHand(player1, List.of(new FeedTheCycle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("forage");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a land target")
    void rejectsLandTarget() {
        Permanent food = addFoodToken(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new FeedTheCycle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), food.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forage rejects fewer than three cards without spending mana or exiling cards")
    void rejectsIncompleteGraveyardPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeasonedWarrenguard());
        List<Card> graveyard = List.of(new Plains(), new SeasonedWarrenguard(), new Plains());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new FeedTheCycle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, target.getId(), List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        harness.assertInHand(player1, "Feed the Cycle");
    }

    @Test
    @DisplayName("Forage cannot exile the same graveyard card more than once")
    void rejectsDuplicateGraveyardPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeasonedWarrenguard());
        List<Card> graveyard = List.of(new Plains(), new SeasonedWarrenguard(), new Plains());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new FeedTheCycle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, target.getId(), List.of(0, 0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Feed the Cycle");
    }

    @Test
    @DisplayName("Cannot forage by sacrificing an opponent's Food")
    void rejectsOpponentsFood() {
        Permanent food = addFoodToken(player2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeasonedWarrenguard());
        harness.setHand(player1, List.of(new FeedTheCycle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), food.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(food, target);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        harness.assertInHand(player1, "Feed the Cycle");
    }

    @Test
    @CardUsed({GoblinElectromancer.class})
    @DisplayName("Cost reduction applies when paying additional black mana instead of foraging")
    void paysReducedManaCostWithoutForaging() {
        harness.addToBattlefield(player1, new GoblinElectromancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeasonedWarrenguard());
        harness.setHand(player1, List.of(new FeedTheCycle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Seasoned Warrenguard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new RalCracklingWit());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addFoodToken(Player player) {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));

        return harness.addToBattlefieldAndReturn(player, food);
    }
}
