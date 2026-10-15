package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CauldronOfEssence.class, GrizzlyBears.class, LlanowarElves.class, Shock.class,
        AngelOfMercy.class, MarchOfTheMachines.class})
class CauldronOfEssenceTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life and controller gains 1 life when a creature you control dies")
    void drainsWhenControlledCreatureDies() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Cauldron trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 1);
    }

    @Test
    @DisplayName("Death trigger fires when the sacrificed creature dies from the activated ability")
    void deathTriggerFiresFromSacrificeCost() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        Card graveyardBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBear));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.activateAbility(player1, 0, 0, null, graveyardBear.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, elvesId);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Returns targeted creature card from graveyard to the battlefield")
    void returnsTargetedCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card graveyardBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBear));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, graveyardBear.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(graveyardBear.getId()));
    }

    @Test
    @DisplayName("Prompts for sacrifice choice when multiple creatures are available")
    void promptsForSacrificeChoice() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        Card angel = new AngelOfMercy();
        harness.setGraveyard(player1, List.of(angel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, angel.getId(), Zone.GRAVEYARD);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureInGraveyard() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, shock.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card angel = new AngelOfMercy();
        harness.setGraveyard(player1, List.of(angel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, angel.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability puts entry on stack")
    void activationPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card angel = new AngelOfMercy();
        harness.setGraveyard(player1, List.of(angel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, angel.getId(), Zone.GRAVEYARD);

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                        && e.getCard().getName().equals("Cauldron of Essence"));
    }

    @Test
    @DisplayName("An animated Cauldron triggers when it dies itself")
    void drainsWhenAnimatedCauldronDies() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        UUID cauldronId = harness.getPermanentId(player1, "Cauldron of Essence");
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player2, 0, cauldronId);
        harness.castAndResolveInstant(player2, 0, cauldronId);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Cauldron of Essence");
        harness.assertNotOnBattlefield(player1, "Cauldron of Essence");
        harness.assertLife(player1, controllerLife + 1);
        harness.assertLife(player2, opponentLife - 1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger the drain")
    void doesNotDrainWhenOpponentsCreatureDies() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, controllerLife);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    @DisplayName("Cannot return a creature card from an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new LlanowarElves());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutSacrifice() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate during combat even on your own turn")
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new CauldronOfEssence());
        harness.addToBattlefield(player1, new LlanowarElves());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
