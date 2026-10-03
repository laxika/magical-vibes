package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.f.FissureWizard;
import com.github.laxika.magicalvibes.cards.k.KorBlademaster;
import com.github.laxika.magicalvibes.cards.m.MerfolkWindrobber;
import com.github.laxika.magicalvibes.cards.p.ProwlingFelidar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfUnity.class, ExpeditionHealer.class, FissureWizard.class,
        KorBlademaster.class, MerfolkWindrobber.class, ProwlingFelidar.class})
class AngelOfUnityTest extends BaseCardTest {

    @Test
    void perpetuallyBoostsAChosenPartyCreatureFromItsEtbTrigger() {
        ExpeditionHealer healerToBoost = new ExpeditionHealer();
        UUID healerToBoostId = healerToBoost.getId();
        harness.setHand(player1, List.of(new AngelOfUnity(), healerToBoost, new ExpeditionHealer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.castCreature(player1, 1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(healerToBoostId))
                .findFirst().orElseThrow();
        assertThat(permanent.getPowerModifier()).isEqualTo(2);
        assertThat(permanent.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void doesNotPromptWhenTheHandHasNoPartyCreature() {
        harness.setHand(player1, List.of(new AngelOfUnity(), new ProwlingFelidar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void offersEveryPartyTypeButExcludesNonpartyCreatures() {
        harness.setHand(player1, List.of(new AngelOfUnity(), new ExpeditionHealer(),
                new MerfolkWindrobber(), new KorBlademaster(), new FissureWizard(),
                new ProwlingFelidar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        var choice = (PendingInteraction.PerpetualPowerToughnessChoice)
                gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1, 2, 3);
        harness.handleCardChosen(player1, 1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 1);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        Permanent rogue = findPermanent(player1, "Merfolk Windrobber");
        assertThat(rogue.getPowerModifier()).isEqualTo(1);
        assertThat(rogue.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForANonpartySpellEvenWithAnEligibleCardInHand() {
        harness.addToBattlefield(player1, new AngelOfUnity());
        harness.setHand(player1, List.of(new ProwlingFelidar(), new ExpeditionHealer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Prowling Felidar");
    }

    @Test
    void castTriggerResolvesBeforeThePartySpellAndSurvivesItsSourceLeaving() {
        harness.addToBattlefield(player1, new AngelOfUnity());
        harness.setHand(player1, List.of(new KorBlademaster(), new ExpeditionHealer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Angel of Unity"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kor Blademaster");
        var choice = (PendingInteraction.PerpetualPowerToughnessChoice)
                gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent healer = findPermanent(player1, "Expedition Healer");
        assertThat(healer.getPowerModifier()).isEqualTo(1);
        assertThat(healer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void perpetualBoostSurvivesEndOfTurnCleanup() {
        harness.setHand(player1, List.of(new AngelOfUnity(), new ExpeditionHealer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent healer = findPermanent(player1, "Expedition Healer");
        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(3);
    }

    @Test
    void castingAWizardBoostsAChosenCard() {
        harness.addToBattlefield(player1, new AngelOfUnity());
        harness.setHand(player1, List.of(new FissureWizard(), new ExpeditionHealer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent healer = findPermanent(player1, "Expedition Healer");
        assertThat(healer.getPowerModifier()).isEqualTo(1);
        assertThat(healer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void anOpponentCastingAPartySpellDoesNotTriggerTheAngel() {
        harness.addToBattlefield(player1, new AngelOfUnity());
        harness.setHand(player1, List.of(new ExpeditionHealer()));
        harness.setHand(player2, List.of(new KorBlademaster()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Kor Blademaster");
    }
}
