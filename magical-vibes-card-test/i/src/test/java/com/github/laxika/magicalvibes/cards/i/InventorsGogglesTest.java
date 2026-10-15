package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GearsmithProdigy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InventorsGoggles.class, GearsmithProdigy.class, GrizzlyBears.class})
class InventorsGogglesTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent goggles = addGogglesReady(player1);
        goggles.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Accepting the may attaches the Goggles to an Artificer you control")
    void attachesToEnteringArtificerOnAccept() {
        Permanent goggles = addGogglesReady(player1);

        GearsmithProdigy artificer = new GearsmithProdigy();
        harness.setHand(player1, List.of(artificer));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringArtificer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == artificer)
                .findFirst()
                .orElseThrow();
        assertThat(goggles.getAttachedTo()).isEqualTo(enteringArtificer.getId());
    }

    @Test
    @DisplayName("Does not trigger for a non-Artificer creature")
    void doesNotTriggerForNonArtificer() {
        Permanent goggles = addGogglesReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(goggles.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for an Artificer an opponent controls")
    void doesNotTriggerForOpponentsArtificer() {
        Permanent goggles = addGogglesReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        GearsmithProdigy artificer = new GearsmithProdigy();
        harness.setHand(player2, List.of(artificer));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(goggles.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Resolving equip attaches the Goggles to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent goggles = addGogglesReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(goggles.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addGogglesReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new InventorsGoggles());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Declining the attachment leaves the Goggles on the original creature")
    void decliningAttachmentKeepsOriginalHost() {
        Permanent originalHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent goggles = addGogglesReady(player1);
        goggles.setAttachedTo(originalHost.getId());
        harness.enterBattlefieldAndReturn(player1, new GearsmithProdigy());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(goggles.getAttachedTo()).isEqualTo(originalHost.getId());
        assertThat(gqs.getEffectivePower(gd, originalHost)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, originalHost)).isEqualTo(4);
    }

    @Test
    @DisplayName("Accepting the attachment moves the boost to the entering Artificer")
    void acceptingAttachmentMovesBoost() {
        Permanent originalHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent goggles = addGogglesReady(player1);
        goggles.setAttachedTo(originalHost.getId());
        Permanent artificer = harness.enterBattlefieldAndReturn(player1, new GearsmithProdigy());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goggles.getAttachedTo()).isEqualTo(artificer.getId());
        assertThat(gqs.getEffectivePower(gd, originalHost)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, originalHost)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, artificer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artificer)).isEqualTo(4);
    }

    @Test
    @DisplayName("An old attachment trigger cannot attach Goggles that left and returned")
    void oldTriggerCannotAttachReturnedGoggles() {
        Permanent goggles = addGogglesReady(player1);
        harness.enterBattlefieldAndReturn(player1, new GearsmithProdigy());

        gd.playerBattlefields.get(player1.getId()).remove(goggles);
        harness.setExile(player1, List.of(goggles.getCard()));
        harness.setExile(player1, List.of());
        Permanent returnedGoggles = harness.enterBattlefieldAndReturn(player1, goggles.getCard());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(returnedGoggles.getId()).isNotEqualTo(goggles.getId());
        assertThat(returnedGoggles.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The Goggles stay attached if the entering Artificer leaves before resolution")
    void enteringArtificerLeavesBeforeResolution() {
        Permanent originalHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent goggles = addGogglesReady(player1);
        goggles.setAttachedTo(originalHost.getId());
        Permanent artificer = harness.enterBattlefieldAndReturn(player1, new GearsmithProdigy());
        gd.playerBattlefields.get(player1.getId()).remove(artificer);
        harness.setExile(player1, List.of(artificer.getCard()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goggles.getAttachedTo()).isEqualTo(originalHost.getId());
    }
}
