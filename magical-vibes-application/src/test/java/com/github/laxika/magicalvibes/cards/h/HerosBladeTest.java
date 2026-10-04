package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.k.KodamaOfTheNorthTree;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HerosBlade.class, GrizzlyBears.class, IsamaruHoundOfKonda.class, KodamaOfTheNorthTree.class})
class HerosBladeTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusThreePlusTwo() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void equipAttachesBladeToCreatureYouControl() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void acceptingMayAttachesBladeToEnteringLegendaryCreature() {
        Permanent blade = addBladeReady(player1);

        harness.setHand(player1, List.of(new IsamaruHoundOfKonda()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent isamaru = findPermanent(player1, "Isamaru, Hound of Konda");
        assertThat(blade.getAttachedTo()).isEqualTo(isamaru.getId());
        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, isamaru)).isEqualTo(4);
    }

    @Test
    void decliningMayLeavesBladeUnattached() {
        Permanent blade = addBladeReady(player1);

        harness.setHand(player1, List.of(new IsamaruHoundOfKonda()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    void doesNotTriggerForNonlegendaryCreature() {
        addBladeReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerForLegendaryCreatureOpponentControls() {
        Permanent blade = addBladeReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new IsamaruHoundOfKonda()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    void triggeredAttachmentCanAttachToCreatureWithShroud() {
        Permanent blade = addBladeReady(player1);
        harness.setHand(player1, List.of(new KodamaOfTheNorthTree()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent kodama = findPermanent(player1, "Kodama of the North Tree");
        assertThat(blade.getAttachedTo()).isEqualTo(kodama.getId());
        assertThat(gqs.getEffectivePower(gd, kodama)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, kodama)).isEqualTo(6);
    }

    @Test
    void acceptingMayMovesBladeFromPreviouslyEquippedCreature() {
        Permanent previous = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(previous.getId());
        harness.setHand(player1, List.of(new IsamaruHoundOfKonda()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blade.getAttachedTo()).isEqualTo(findPermanent(player1, "Isamaru, Hound of Konda").getId());
        assertThat(gqs.getEffectivePower(gd, previous)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, previous)).isEqualTo(2);
    }

    @Test
    void decliningMayKeepsBladeOnPreviouslyEquippedCreature() {
        Permanent previous = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(previous.getId());
        harness.setHand(player1, List.of(new IsamaruHoundOfKonda()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blade.getAttachedTo()).isEqualTo(previous.getId());
        assertThat(gqs.getEffectivePower(gd, previous)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, previous)).isEqualTo(4);
    }
    private Permanent addBladeReady(Player player) {
        return addCreatureReady(player, new HerosBlade());
    }

}
