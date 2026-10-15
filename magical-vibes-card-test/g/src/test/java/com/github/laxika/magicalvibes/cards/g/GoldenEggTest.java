package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldenEgg.class, Island.class})
class GoldenEggTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and draws a card")
    void entersAndDrawsCard() {
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.setHand(player1, List.of(new GoldenEgg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Golden Egg");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Sacrifices to add one mana of any color")
    void sacrificesForMana() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(egg);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(egg.getCard());
    }

    @Test
    @DisplayName("Sacrifices to gain 3 life")
    void sacrificesForLife() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(egg);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(egg.getCard());
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Mana ability pays its costs and resolves without using the stack for every color")
    void manaAbilityResolvesImmediately(ManaColor color) {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(egg.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Golden Egg");
        harness.assertInGraveyard(player1, "Golden Egg");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
    }

    @Test
    @DisplayName("Life ability sacrifices immediately but gains life only when it resolves")
    void lifeAbilityUsesStack() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 14);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(egg.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Golden Egg");
        harness.assertInGraveyard(player1, "Golden Egg");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither ability can be activated while Golden Egg is tapped")
    void tappedEggCannotActivate(int abilityIndex) {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        egg.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Golden Egg");
        harness.assertNotInGraveyard(player1, "Golden Egg");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither ability can be activated without enough mana to pay its cost")
    void insufficientManaDoesNotSacrificeEgg(int abilityIndex) {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        harness.addMana(player1, ManaColor.COLORLESS, abilityIndex);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(egg.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Golden Egg");
        harness.assertNotInGraveyard(player1, "Golden Egg");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(abilityIndex);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The enter trigger still draws exactly one card after Golden Egg is sacrificed")
    void enterTriggerSurvivesSacrifice() {
        Island firstCard = new Island();
        Island secondCard = new Island();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new GoldenEgg()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Golden Egg");
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        harness.assertNotOnBattlefield(player1, "Golden Egg");
    }
}
