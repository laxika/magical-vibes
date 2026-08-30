package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OsteomancerAdept.class, GrizzlyBears.class})
class OsteomancerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a creature from the graveyard by exiling three cards and gives it finality")
    void castsCreatureByForagingFromGraveyard() {
        Permanent adept = addReadyAdept();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), bear));
        grantPermission(adept);
        addBearMana();

        harness.castFromGraveyard(player1, 3, List.of(0, 1, 2));
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bear.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, entered));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bear.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bear.getId()));
    }

    @Test
    @DisplayName("Casts a creature from the graveyard by sacrificing a Food")
    void castsCreatureBySacrificingFood() {
        Permanent adept = addReadyAdept();
        Permanent food = addFoodToken();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        grantPermission(adept);
        addBearMana();

        harness.getGameService().playFlashbackSpell(
                gd, player1, 0, null, null, List.of(), null, null, List.of(), null, food.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent == food);
        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bear.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot replace mandatory foraging with mana")
    void cannotPayManaInsteadOfForaging() {
        Permanent adept = addReadyAdept();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        grantPermission(adept);
        addBearMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("forage");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bear);
    }

    private Permanent addReadyAdept() {
        Permanent adept = new Permanent(new OsteomancerAdept());
        adept.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(adept);
        return adept;
    }

    private void grantPermission(Permanent adept) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(adept);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }

    private void addBearMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Permanent addFoodToken() {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));

        Permanent permanent = new Permanent(food);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
