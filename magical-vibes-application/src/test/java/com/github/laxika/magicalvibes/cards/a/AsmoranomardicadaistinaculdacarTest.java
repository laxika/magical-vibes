package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FieryEmancipation;
import com.github.laxika.magicalvibes.cards.h.HealersFlock;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.t.TheUnderworldCookbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Asmoranomardicadaistinaculdacar.class, OrnithopterOfParadise.class,
        TheUnderworldCookbook.class, FieryEmancipation.class, HealersFlock.class})
class AsmoranomardicadaistinaculdacarTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for {B/R} after discarding a card and searches for The Underworld Cookbook")
    void castsAfterDiscardingAndSearchesForCookbook() {
        harness.setHand(player1, List.of(new Asmoranomardicadaistinaculdacar()));
        harness.setLibrary(player1, List.of(new TheUnderworldCookbook(), new OrnithopterOfParadise()));
        gd.cardsDiscardedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Asmoranomardicadaistinaculdacar");
        harness.assertInHand(player1, "The Underworld Cookbook");
    }

    @Test
    @DisplayName("The alternate cost requires a discard this turn")
    void alternateCostRequiresDiscard() {
        harness.setHand(player1, List.of(new Asmoranomardicadaistinaculdacar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing two Foods makes a target creature deal 6 damage to itself")
    void sacrificesTwoFoodsToDestroyTargetCreature() {
        Permanent asmoranomardicadaistinaculdacar =
                harness.addToBattlefieldAndReturn(player1, new Asmoranomardicadaistinaculdacar());
        harness.addToBattlefield(player1, foodToken());
        harness.addToBattlefield(player1, foodToken());
        harness.addToBattlefield(player1, foodToken());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(asmoranomardicadaistinaculdacar),
                null,
                target.getId());
        List<Permanent> foods = findPermanents(player1, "Food");
        harness.handlePermanentChosen(player1, foods.get(0).getId());
        harness.handlePermanentChosen(player1, foods.get(1).getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertNotOnBattlefield(player2, "Ornithopter of Paradise");
    }

    @Test
    void cannotBypassHybridCostByCastingNormally() {
        harness.setHand(player1, List.of(new Asmoranomardicadaistinaculdacar()));
        gd.cardsDiscardedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Asmoranomardicadaistinaculdacar");
    }

    @Test
    void canPayAlternateCostWithRedAndDeclineSearch() {
        harness.setHand(player1, List.of(new Asmoranomardicadaistinaculdacar()));
        TheUnderworldCookbook cookbook = new TheUnderworldCookbook();
        harness.setLibrary(player1, List.of(cookbook));
        gd.cardsDiscardedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Asmoranomardicadaistinaculdacar");
        harness.assertNotInHand(player1, "The Underworld Cookbook");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cookbook);
    }

    @Test
    void opponentsDiscardDoesNotEnableAlternateCost() {
        harness.setHand(player1, List.of(new Asmoranomardicadaistinaculdacar()));
        gd.cardsDiscardedThisTurn.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void acceptedSearchCanFailToFindCookbook() {
        harness.setHand(player1, List.of(new Asmoranomardicadaistinaculdacar()));
        harness.setLibrary(player1, List.of(new TheUnderworldCookbook()));
        gd.cardsDiscardedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "The Underworld Cookbook");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPaySacrificeCostWithOpponentsFood() {
        harness.addToBattlefield(player1, new Asmoranomardicadaistinaculdacar());
        harness.addToBattlefield(player1, foodToken());
        harness.addToBattlefield(player2, foodToken());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player2, "Food")).isOne();
    }

    @Test
    void canTargetItsOwnControllerCreature() {
        harness.addToBattlefield(player1, new Asmoranomardicadaistinaculdacar());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        harness.addToBattlefield(player1, foodToken());
        harness.addToBattlefield(player1, foodToken());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter of Paradise");
        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    void damageMultiplierUsesTargetControllerRatherThanAbilityController() {
        harness.addToBattlefield(player1, new Asmoranomardicadaistinaculdacar());
        harness.addToBattlefield(player1, new FieryEmancipation());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());
        harness.addToBattlefield(player1, foodToken());
        harness.addToBattlefield(player1, foodToken());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    void targetControllersDamageMultiplierAppliesToSelfDamage() {
        harness.addToBattlefield(player1, new Asmoranomardicadaistinaculdacar());
        harness.addToBattlefield(player2, new FieryEmancipation());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());
        harness.addToBattlefield(player1, foodToken());
        harness.addToBattlefield(player1, foodToken());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(18);
    }

    @Test
    void targetsLifelinkGainsLifeForItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Asmoranomardicadaistinaculdacar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HealersFlock());
        harness.addToBattlefield(player1, foodToken());
        harness.addToBattlefield(player1, foodToken());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();

        harness.assertLife(player2, 26);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Healer's Flock");
    }

    private Card foodToken() {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));
        return food;
    }
}
