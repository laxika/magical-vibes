package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.o.OriginSpellbomb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SalvageScout.class, OriginSpellbomb.class, Memnite.class})
class SalvageScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices Salvage Scout and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new OriginSpellbomb()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Salvage Scout");
        harness.assertInGraveyard(player1, "Salvage Scout");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        harness.assertNotInHand(player1, "Origin Spellbomb");
    }

    @Test
    @DisplayName("Activating ability consumes {W} mana")
    void activatingAbilityConsumesMana() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new OriginSpellbomb()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns artifact from graveyard to hand")
    void returnsArtifactFromGraveyardToHand() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new OriginSpellbomb()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Origin Spellbomb");
        harness.assertNotInGraveyard(player1, "Origin Spellbomb");
    }

    @Test
    @DisplayName("Choosing specific artifact when multiple are in graveyard")
    void choosesSpecificArtifactFromGraveyard() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new OriginSpellbomb(), new Memnite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).get(1).getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Memnite");
        harness.assertInGraveyard(player1, "Origin Spellbomb");
    }

    @Test
    @DisplayName("Cannot target a non-artifact card during activation")
    void cannotChooseNonArtifactFromGraveyard() {
        addScoutToBattlefield(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        SalvageScout nonArtifact = new SalvageScout();
        harness.setGraveyard(player1, List.of(nonArtifact, new OriginSpellbomb()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Salvage Scout");
    }

    @Test
    @DisplayName("Cannot activate without a legal artifact target")
    void scoutItselfNotValidChoice() {
        addScoutToBattlefield(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Salvage Scout");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate with summoning sickness since ability does not require tap")
    void canActivateWithSummoningSickness() {
        SalvageScout card = new SalvageScout();
        harness.addToBattlefield(player1, card);
        harness.setGraveyard(player1, List.of(new OriginSpellbomb()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Can activate while tapped since ability does not require tap")
    void canActivateWhileTapped() {
        Permanent scout = addScoutToBattlefield(player1);
        scout.tap();
        harness.setGraveyard(player1, List.of(new OriginSpellbomb()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new OriginSpellbomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target an artifact in an opponent graveyard")
    void opponentCannotChoose() {
        addScoutToBattlefield(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        OriginSpellbomb artifact = new OriginSpellbomb();
        harness.setGraveyard(player2, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Salvage Scout");
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new OriginSpellbomb()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, gd.playerGraveyards.get(player1.getId()).getFirst().getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An artifact target that leaves the graveyard is not replaced during resolution")
    void removedTargetDoesNotReturnAnotherArtifact() {
        addScoutToBattlefield(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        OriginSpellbomb target = new OriginSpellbomb();
        harness.setGraveyard(player1, List.of(target, new Memnite()));
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player1.getId()).remove(target);
        gd.playerHands.get(player1.getId()).add(target);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Memnite");
        harness.assertNotInHand(player1, "Memnite");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addScoutToBattlefield(Player player) {
        Permanent scout = harness.addToBattlefieldAndReturn(player, new SalvageScout());
        scout.setSummoningSick(false);
        return scout;
    }
}
