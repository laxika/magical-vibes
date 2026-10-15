package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.r.RoverBlades;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WinterCursedRider.class, Disenchant.class, DarksteelRelic.class,
        HillGiant.class, Ornithopter.class, Shock.class, RoverBlades.class})
class WinterCursedRiderTest extends BaseCardTest {

    @Test
    void wardProtectsWinterAndCanBePaidWithLife() {
        Permanent winter = addCreatureReady(player1, new WinterCursedRider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player2, 0, winter.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Winter, Cursed Rider");
        harness.assertInGraveyard(player1, "Winter, Cursed Rider");
    }

    @Test
    void artifactsYouControlHaveWard() {
        addCreatureReady(player1, new WinterCursedRider());
        Permanent relic = addCreatureReady(player1, new Ornithopter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player2, 0, relic.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void exhaustExilesArtifactCardsAndDebuffsOtherNonartifactCreatures() {
        Permanent winter = addCreatureReady(player1, new WinterCursedRider());
        Permanent ownGiant = addCreatureReady(player1, new HillGiant());
        Permanent artifactCreature = addCreatureReady(player1, new Ornithopter());
        Permanent opposingGiant = addCreatureReady(player2, new HillGiant());
        Ornithopter artifactInGraveyard = new Ornithopter();
        DarksteelRelic relicInGraveyard = new DarksteelRelic();
        Shock nonartifactInGraveyard = new Shock();
        harness.setGraveyard(player1, List.of(artifactInGraveyard, relicInGraveyard, nonartifactInGraveyard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 2, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifactInGraveyard.getId(), relicInGraveyard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifactInGraveyard, relicInGraveyard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonartifactInGraveyard);
        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, winter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, winter)).isEqualTo(2);
    }

    @Test
    void exhaustAbilityCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new WinterCursedRider());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getFirst().untap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void decliningWardCountersTheSpell() {
        Permanent winter = addCreatureReady(player1, new WinterCursedRider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player2, 0, winter.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Winter, Cursed Rider");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, lifeBefore);
        assertThat(winter.getMarkedDamage()).isZero();
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        Permanent artifact = addCreatureReady(player1, new Ornithopter());
        addCreatureReady(player1, new WinterCursedRider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void opponentsArtifactsDoNotGainWard() {
        addCreatureReady(player1, new WinterCursedRider());
        Permanent artifact = addCreatureReady(player2, new Ornithopter());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void exhaustCanExileZeroCardsAndStillConsumesTheActivation() {
        Permanent winter = addCreatureReady(player1, new WinterCursedRider());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        Ornithopter artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(winter.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        winter.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void exhaustAffectsOnlyCreaturesPresentAtResolutionAndEndsAtCleanup() {
        addCreatureReady(player1, new WinterCursedRider());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        Ornithopter artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        resolveAllTriggers();

        Permanent lateGiant = addCreatureReady(player2, new HillGiant());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, lateGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lateGiant)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    void attachedArtifactHasWardWhenItIsTargetedDirectly() {
        addCreatureReady(player1, new WinterCursedRider());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new RoverBlades());
        equipment.setAttachedTo(giant.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, equipment.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Rover Blades");
        harness.assertInGraveyard(player2, "Disenchant");
    }

    @Test
    void attachedArtifactsWardDoesNotProtectTheEquippedCreature() {
        addCreatureReady(player1, new WinterCursedRider());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new RoverBlades());
        equipment.setAttachedTo(giant.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player2, 0, giant.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, lifeBefore);
    }
}
