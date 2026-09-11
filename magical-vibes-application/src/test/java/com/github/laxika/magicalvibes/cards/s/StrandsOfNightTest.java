package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherFlash;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherFlash.class, BenalishKnight.class, Forest.class, GrizzlyBears.class, StrandsOfNight.class, Swamp.class})
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
        Card creature = new BenalishKnight();
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "Swamp");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
