package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NipGwyllion;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathbringerLiege.class, GrizzlyBears.class, SuntailHawk.class, DiregrafGhoul.class,
        NipGwyllion.class})
class DeathbringerLiegeTest extends BaseCardTest {


    @Test
    @DisplayName("Other white creatures you control get +1/+1")
    void buffsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Other black creatures you control get +1/+1")
    void buffsOwnBlackCreatures() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new DiregrafGhoul());
        assertThat(gqs.getEffectivePower(gd, ghoul)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ghoul)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-white non-black creatures")
    void doesNotBuffOffColorCreatures() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }


    @Test
    @DisplayName("Casting a white spell offers the tap-target may ability")
    void whiteSpellTriggersTap() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID bearsId = bears.getId();

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A black spell does not fire the white tap trigger")
    void blackSpellDoesNotFireWhiteTrigger() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DiregrafGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        // Black spell fires only the destroy trigger; the untapped bear survives resolution.
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }


    @Test
    @DisplayName("Casting a black spell destroys a tapped target creature")
    void blackSpellDestroysTappedCreature() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID bearsId = bears.getId();
        bears.tap();

        harness.setHand(player1, List.of(new DiregrafGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An untapped target survives the black spell trigger")
    void blackSpellSparesUntappedCreature() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new DiregrafGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void bothColorBonusesApplyOnlyToOtherCreaturesYouControl() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new DeathbringerLiege());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new NipGwyllion());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new NipGwyllion());

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(4);
    }

    @Test
    void opponentCastingDualColorSpellDoesNotTriggerLiege() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new DeathbringerLiege());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new NipGwyllion()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(liege.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Nip Gwyllion");
    }

    @Test
    void whiteTriggerChoosesTargetBeforeOfferingResolutionTimeMayChoice() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Deathbringer Liege"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Deathbringer Liege").isTapped()).isFalse();
    }

    @Test
    void blackTriggerChoosesTargetBeforeOfferingResolutionTimeMayChoice() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NipGwyllion());
        target.tap();
        harness.setHand(player1, List.of(new DiregrafGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Nip Gwyllion");
    }

    @Test
    void dualColorSpellTriggersBothAbilitiesEvenWhenPaidWithWhiteMana() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new DeathbringerLiege());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NipGwyllion());
        target.tap();
        harness.setHand(player1, List.of(new NipGwyllion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        PendingInteraction.ColorChoice order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        harness.handleListChoice(player1, order.options().getFirst());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        harness.assertInGraveyard(player2, "Nip Gwyllion");
        harness.assertOnBattlefield(player1, "Nip Gwyllion");
        assertThat(liege.isTapped()).isFalse();
    }
}
