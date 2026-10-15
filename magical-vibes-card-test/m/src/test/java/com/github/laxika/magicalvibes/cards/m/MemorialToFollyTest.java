package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemorialToFolly.class, BalothGorger.class, PrimordialWurm.class, Divination.class})
class MemorialToFollyTest extends BaseCardTest {

    @Test
    @DisplayName("Memorial to Folly enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new MemorialToFolly()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent memorial = findPermanent(player1, "Memorial to Folly");
        assertThat(memorial.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Memorial to Folly produces black mana")
    void tappingProducesBlackMana() {
        Permanent memorial = addMemorialReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(memorial);

        harness.tapPermanent(player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability sacrifices Memorial to Folly and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addMemorialReady(player1);
        harness.setGraveyard(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);

        // Memorial should be sacrificed (not on battlefield, in graveyard)
        harness.assertNotOnBattlefield(player1, "Memorial to Folly");
        harness.assertInGraveyard(player1, "Memorial to Folly");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Memorial to Folly");
    }

    @Test
    @DisplayName("Activating ability consumes {2}{B} mana")
    void activatingAbilityConsumesMana() {
        addMemorialReady(player1);
        harness.setGraveyard(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);

        // Should have 1 black mana remaining (4 - 3 for ability cost)
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns creature from graveyard to hand")
    void returnsCreatureFromGraveyardToHand() {
        addMemorialReady(player1);
        harness.setGraveyard(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();

        // Baloth Gorger moved from graveyard to hand
        harness.assertInHand(player1, "Baloth Gorger");
        harness.assertNotInGraveyard(player1, "Baloth Gorger");
    }

    @Test
    @DisplayName("Choosing specific creature when multiple are in graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        addMemorialReady(player1);
        harness.setGraveyard(player1, List.of(new BalothGorger(), new PrimordialWurm()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).get(1).getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertInHand(player1, "Primordial Wurm");
        // Baloth Gorger stays in graveyard
        harness.assertInGraveyard(player1, "Baloth Gorger");
        harness.assertNotInGraveyard(player1, "Primordial Wurm");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addMemorialReady(player1);
        harness.setGraveyard(player1, List.of(new BalothGorger()));
        // No mana added
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent memorial = addMemorialReady(player1);
        memorial.tap();
        harness.setGraveyard(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNonCreatureFromGraveyard() {
        addMemorialReady(player1);
        harness.setGraveyard(player1, List.of(new Divination(), new BalothGorger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Memorial to Folly");
    }

    @Test
    @DisplayName("Cannot target a creature in the opponent's graveyard")
    void cannotTargetOpponentsCreature() {
        addMemorialReady(player1);
        harness.setGraveyard(player2, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                gd.playerGraveyards.get(player2.getId()).getFirst().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Memorial to Folly");
    }

    @Test
    @DisplayName("Cannot activate without a target")
    void cannotActivateWithoutTarget() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Memorial to Folly");
    }

    @Test
    @DisplayName("An ability whose target leaves the graveyard cannot return another creature")
    void removedTargetDoesNotAllowAnotherChoice() {
        addMemorialReady(player1);
        BalothGorger target = new BalothGorger();
        harness.setGraveyard(player1, List.of(target, new PrimordialWurm()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player1.getId()).remove(target);
        gd.addToExile(player1.getId(), target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Baloth Gorger");
        harness.assertNotInHand(player1, "Primordial Wurm");
        harness.assertInGraveyard(player1, "Primordial Wurm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        addMemorialReady(player1);
        harness.setGraveyard(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addMemorialReady(Player player) {
        Permanent memorial = harness.addToBattlefieldAndReturn(player, new MemorialToFolly());
        memorial.setSummoningSick(false);
        return memorial;
    }
}
