package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CauldronOfEssenceTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ALLY_CREATURE_DIES effects for opponent life loss and controller life gain")
    void hasCorrectDeathTriggerStructure() {
        CauldronOfEssence card = new CauldronOfEssence();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).get(0))
                .isInstanceOf(EachOpponentLosesLifeEffect.class);
        assertThat(((EachOpponentLosesLifeEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).get(0)).amount())
                .isEqualTo(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).get(1))
                .isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).get(1)).amount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Has sorcery-speed activated ability with sacrifice cost and graveyard return")
    void hasCorrectActivatedAbility() {
        CauldronOfEssence card = new CauldronOfEssence();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{1}{B}{G}");
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect returnEffect =
                (ReturnCardFromGraveyardEffect) ability.getEffects().get(1);
        assertThat(returnEffect.targetGraveyard()).isTrue();
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("Each opponent loses 1 life and controller gains 1 life when a creature you control dies")
    void drainsWhenControlledCreatureDies() {
        addReadyCauldron(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → Bears die → Cauldron trigger
        harness.passBothPriorities(); // Resolve Cauldron trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 1);
    }

    @Test
    @DisplayName("Death trigger fires when the sacrificed creature dies from the activated ability")
    void deathTriggerFiresFromSacrificeCost() {
        addReadyCauldron(player1);
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
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Returns targeted creature card from graveyard to the battlefield")
    void returnsTargetedCreatureFromGraveyard() {
        addReadyCauldron(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card graveyardBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBear));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, graveyardBear.getId(), Zone.GRAVEYARD);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(graveyardBear.getId()));
    }

    @Test
    @DisplayName("Prompts for sacrifice choice when multiple creatures are available")
    void promptsForSacrificeChoice() {
        addReadyCauldron(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        Card angel = new com.github.laxika.magicalvibes.cards.a.AngelOfMercy();
        harness.setGraveyard(player1, List.of(angel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, angel.getId(), Zone.GRAVEYARD);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureInGraveyard() {
        addReadyCauldron(player1);
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
        addReadyCauldron(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card angel = new com.github.laxika.magicalvibes.cards.a.AngelOfMercy();
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
        addReadyCauldron(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card angel = new com.github.laxika.magicalvibes.cards.a.AngelOfMercy();
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

    // ===== Helpers =====

    private Permanent addReadyCauldron(Player player) {
        CauldronOfEssence card = new CauldronOfEssence();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
