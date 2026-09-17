package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({SquirrelNest.class, Forest.class})
class SquirrelNestTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's granted ability creates a 1/1 green Squirrel token")
    void grantedAbilityCreatesSquirrelToken() {
        Permanent forest = setUpEnchantedForest();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement()
                .satisfies(squirrel -> {
                    assertThat(squirrel.getCard().getName()).isEqualTo("Squirrel");
                    assertThat(squirrel.getCard().getPower()).isEqualTo(1);
                    assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
                    assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(squirrel.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(squirrel.getCard().getSubtypes()).containsExactly(CardSubtype.SQUIRREL);
                });
    }

    @Test
    @DisplayName("A tapped enchanted land cannot activate the granted ability")
    void tappedLandCannotActivate() {
        Permanent forest = setUpEnchantedForest();
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Squirrel Nest's ability is usable by the enchanted land's controller")
    void enchantedLandControllerCanActivateAbility() {
        Permanent forest = setUpEnchantedForest(player2, player1);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(forest), null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Squirrel"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Squirrel Nest attaches to the target land when it resolves")
    void resolvingSquirrelNestAttachesToLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SquirrelNest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Squirrel Nest")
                        && forest.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Removing Squirrel Nest removes the granted ability")
    void removingSquirrelNestRemovesGrantedAbility() {
        Permanent forest = setUpEnchantedForest();
        Permanent aura = findPermanent(player1, "Squirrel Nest");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability resolves after Squirrel Nest leaves the battlefield")
    void activatedAbilityResolvesAfterSquirrelNestLeaves() {
        Permanent forest = setUpEnchantedForest();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Squirrel Nest"));
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Squirrel"));
    }

    @Test
    @DisplayName("Squirrel Nest can enchant only a land")
    void cannotEnchantNonLandPermanent() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new SquirrelNest());
        harness.setHand(player1, List.of(new SquirrelNest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent setUpEnchantedForest() {
        return setUpEnchantedForest(player1, player1);
    }

    private Permanent setUpEnchantedForest(Player landController, Player auraController) {
        Permanent forest = harness.addToBattlefieldAndReturn(landController, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new SquirrelNest());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
