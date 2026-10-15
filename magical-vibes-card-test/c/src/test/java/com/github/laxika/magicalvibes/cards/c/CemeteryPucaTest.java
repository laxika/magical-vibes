package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FaerieMacabre;
import com.github.laxika.magicalvibes.cards.m.MercyKilling;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Scar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TattermungeManiac;
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

@CardUsed({CemeteryPuca.class, GrizzlyBears.class, RuneclawBear.class, Shock.class,
        FaerieMacabre.class, MercyKilling.class, Scar.class, TattermungeManiac.class})
class CemeteryPucaTest extends BaseCardTest {

    @Test
    void copiesDeadTokenWithoutBecomingToken() {
        Permanent puca = putPuca();
        Permanent maniac = harness.addToBattlefieldAndReturn(player2, new TattermungeManiac());
        harness.setHand(player1, List.of(new MercyKilling(), new Scar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, maniac.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.handleMayAbilityChosen(player1, false);
        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, token.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(puca.getCard().getName()).isEqualTo("Elf Warrior");
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(1);
        assertThat(puca.getCard().isToken()).isFalse();
    }

    @Test
    void copiesCreatureEvenAfterItsCardIsExiled() {
        Permanent puca = putPuca();
        Permanent maniac = harness.addToBattlefieldAndReturn(player2, new TattermungeManiac());
        harness.setHand(player1, List.of(new Scar(), new FaerieMacabre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, maniac.getId()));

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(maniac.getCard().getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(puca.getCard().getName()).isEqualTo("Tattermunge Maniac");
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(1);
    }

    @Test
    void copiesCopiableCharacteristicsOfCreatureThatWasAlreadyACopy() {
        Permanent firstPuca = putPuca();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, bears.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        Permanent secondPuca = putPuca();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, firstPuca.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(secondPuca.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, secondPuca)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondPuca)).isEqualTo(2);
    }

    @Test
    void paymentChoiceWaitsUntilTriggerResolves() {
        putPuca();
        Permanent maniac = harness.addToBattlefieldAndReturn(player2, new TattermungeManiac());
        harness.setHand(player1, List.of(new Scar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, maniac.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent putPuca() {
        return harness.addToBattlefieldAndReturn(player1, new CemeteryPuca());
    }

    @Test
    @DisplayName("Pays {1} to become a copy of a creature that died")
    void becomesCopyWhenPaid() {
        Permanent puca = putPuca();

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1); // for the {1}
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, bears.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities); // become-copy resolves

        assertThat(puca.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining to pay leaves it as Cemetery Puca")
    void staysWhenDeclined() {
        Permanent puca = putPuca();

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, bears.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(puca.getCard().getName()).isEqualTo("Cemetery Puca");
    }

    @Test
    @DisplayName("Retains the copy ability and copies again when another creature dies")
    void retainsAbilityAndCopiesAgain() {
        Permanent puca = putPuca();

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent runeclaw = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        // First death: copy Grizzly Bears
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, bears.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(puca.getCard().getName()).isEqualTo("Grizzly Bears");

        // Second death: the copy still has the trigger, so it fires again and copies Runeclaw Bear
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castAndResolveInstant(player1, 0, runeclaw.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(puca.getCard().getName()).isEqualTo("Runeclaw Bear");
    }
}
