package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherFlash;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrandsOfNight.class, BenalishKnight.class, AetherFlash.class,
        Swamp.class, WindingCanyons.class})
class StrandsOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature from your graveyard, paying 2 life and sacrificing a Swamp")
    void returnsCreatureToBattlefield() {
        Card creature = new BenalishKnight();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benalish Knight");
        harness.assertNotInGraveyard(player1, "Benalish Knight");
        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cannot activate without a Swamp to sacrifice")
    void cannotActivateWithoutSwamp() {
        Card creature = new BenalishKnight();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp");
    }

    @Test
    @DisplayName("Cannot activate with a land that is not a Swamp")
    void cannotActivateWithNonSwampLand() {
        Card creature = new BenalishKnight();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp");

        harness.assertOnBattlefield(player1, "Winding Canyons");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a non-creature card in your graveyard")
    void cannotTargetNonCreature() {
        Card nonCreature = new AetherFlash();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new BenalishKnight();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot activate when you have less than 2 life")
    void cannotActivateWithoutEnoughLife() {
        Card creature = new BenalishKnight();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("Does not return the target if it leaves the graveyard before resolution")
    void targetLeavingGraveyardBeforeResolutionFizzles() {
        Card creature = new BenalishKnight();
        Swamp swamp = new Swamp();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, swamp);
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));

        harness.setHand(player1, List.of(creature));
        harness.setGraveyard(player1, List.of(swamp));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Benalish Knight");
        harness.assertInHand(player1, "Benalish Knight");
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertLife(player1, 18);
    }
}
