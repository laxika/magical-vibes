package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherFlash;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherFlash.class, Disenchant.class, Forest.class, GrizzlyBears.class, Island.class, StrandsOfNight.class, Swamp.class})
class StrandsOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Swamp can be sacrificed and all costs are paid before resolution")
    void paysCostsBeforeReturningCreature() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefieldAndReturn(player1, new Swamp()).tap();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isFalse());
    }

    @Test
    @DisplayName("The activated ability resolves after Strands of Night is destroyed")
    void resolvesAfterSourceIsDestroyed() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Strands of Night"));

        harness.assertNotOnBattlefield(player1, "Strands of Night");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returning a creature triggers Aether Flash")
    void returnedCreatureTriggersEntersBattlefieldAbility() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new AetherFlash());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Chooses which Swamp to sacrifice when multiple Swamps are controlled")
    void choosesSwampToSacrifice() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        var firstSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        var chosenSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.handlePermanentChosen(player1, chosenSwamp.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstSwamp).doesNotContain(chosenSwamp);
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns target creature from your graveyard, paying 2 life and sacrificing a Swamp")
    void returnsCreatureToBattlefield() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cannot activate without a Swamp to sacrifice")
    void cannotActivateWithoutSwamp() {
        Card creature = new GrizzlyBears();
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
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Island());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp");

        harness.assertOnBattlefield(player1, "Island");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot sacrifice a Swamp controlled by an opponent")
    void cannotSacrificeOpponentsSwamp() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player2, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp");

        harness.assertOnBattlefield(player2, "Swamp");
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
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate when you have less than 2 life")
    void cannotActivateWithoutEnoughLife() {
        Card creature = new GrizzlyBears();
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
        Card creature = new GrizzlyBears();
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

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cannot use a Forest to pay the Swamp sacrifice cost")
    void cannotActivateWithForestInsteadOfSwamp() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Forest());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp");

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without two black mana")
    void cannotActivateWithoutEnoughBlackMana() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate when unable to pay 2 life")
    void cannotActivateWithInsufficientLife() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fizzles if the targeted creature leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card.getId().equals(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "Swamp");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
