package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.y.YouComeToARiver;
import com.github.laxika.magicalvibes.cards.y.YouFindTheVillainsLair;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernoOfTheStarMounts.class, YouComeToARiver.class, YouFindTheVillainsLair.class})
class InfernoOfTheStarMountsTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability is untargeted and boosts Inferno")
    void activatedAbilityBoostsInfernoWithoutTargeting() {
        Permanent inferno = addReadyInferno();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The linked ability triggers only when the boost makes power exactly 20")
    void linkedAbilityTriggersAtExactPowerThreshold() {
        Permanent inferno = addReadyInferno();
        inferno.setPowerModifier(13);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 0);
    }

    @Test
    @DisplayName("The linked ability does not trigger when power becomes greater than 20")
    void linkedAbilityDoesNotTriggerPastExactPowerThreshold() {
        Permanent inferno = addReadyInferno();
        inferno.setPowerModifier(14);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(21);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Inferno resolves despite a counterspell targeting it")
    void cannotBeCountered() {
        InfernoOfTheStarMounts inferno = new InfernoOfTheStarMounts();
        harness.setHand(player1, List.of(inferno));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setHand(player2, List.of(new YouFindTheVillainsLair()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, inferno.getId(), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inferno of the Star Mounts");
        harness.assertInGraveyard(player2, "You Find the Villains' Lair");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Reaching power 20 with a different spell does not trigger damage")
    void otherPumpDoesNotTriggerDamage() {
        Permanent inferno = addReadyInferno();
        inferno.setPowerModifier(13);
        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, 1, List.of(inferno.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The triggered damage can target a creature")
    void triggeredDamageCanTargetCreature() {
        Permanent inferno = addReadyInferno();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InfernoOfTheStarMounts());
        inferno.setPowerModifier(13);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Inferno of the Star Mounts");
        harness.assertOnBattlefield(player1, "Inferno of the Star Mounts");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Triggered damage remains 20 if Inferno's power increases in response")
    void triggeredDamageDoesNotRecheckPower() {
        Permanent inferno = addReadyInferno();
        inferno.setPowerModifier(13);
        harness.setLife(player2, 40);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(21);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing Inferno before its boost resolves prevents the damage trigger")
    void sourceRemovedBeforeBoostDoesNotTrigger() {
        Permanent inferno = addReadyInferno();
        inferno.setPowerModifier(13);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new YouComeToARiver()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.castModalInstant(player2, 0, 0, List.of(inferno.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .contains("Inferno of the Star Mounts");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing Inferno after its damage ability triggers does not stop the damage")
    void triggeredDamageSurvivesSourceRemoval() {
        Permanent inferno = addReadyInferno();
        inferno.setPowerModifier(13);
        harness.setLife(player2, 40);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new YouComeToARiver()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passPriority(player1);
        harness.castModalInstant(player2, 0, 0, List.of(inferno.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .contains("Inferno of the Star Mounts");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple boosts leave toughness unchanged and expire at end of turn")
    void boostsAccumulateAndExpire() {
        Permanent inferno = addReadyInferno();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, inferno)).isEqualTo(6);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, inferno)).isEqualTo(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An already tapped Inferno can activate its ability")
    void tappedSourceCanActivateAbility() {
        Permanent inferno = addReadyInferno();
        inferno.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(7);
        assertThat(inferno.isTapped()).isTrue();
    }

    private Permanent addReadyInferno() {
        return addCreatureReady(player1, new InfernoOfTheStarMounts());
    }
}
