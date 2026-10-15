package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MassacreGirl.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class,
        Shock.class, Unsummon.class})
class MassacreGirlTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives each other creature -1/-1 and leaves Massacre Girl unchanged")
    void etbWeakensOtherCreatures() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        castMassacreGirl();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(1);

        Permanent massacreGirl = findPermanent(player1, "Massacre Girl");
        assertThat(massacreGirl.getPowerModifier()).isZero();
        assertThat(massacreGirl.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each creature death causes another -1/-1 trigger")
    void creatureDeathsCauseCascadingWeakness() {
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());

        castMassacreGirl();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(wizard.getCard(), bears.getCard(), hillGiant.getCard());
        Permanent massacreGirl = findPermanent(player1, "Massacre Girl");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(massacreGirl);
    }

    @Test
    @DisplayName("The -1/-1 effects wear off at end of turn")
    void weaknessWearsOffAtEndOfTurn() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        castMassacreGirl();
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Simultaneous deaths each produce a separate weakening")
    void simultaneousDeathsEachTrigger() {
        addCreatureReady(player1, new FugitiveWizard());
        addCreatureReady(player2, new FugitiveWizard());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        castMassacreGirl();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(giant.getCard());
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Massacre Girl"))).isEqualTo(4);
    }

    @Test
    @DisplayName("Later entrants receive only subsequently resolved weakenings")
    void laterEntrantsAreAffectedByLaterDeaths() {
        castMassacreGirl();
        resolveAllTriggers();
        Permanent giant = addCreatureReady(player2, new HillGiant());
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());

        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, wizard.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Massacre Girl"))).isEqualTo(4);
    }

    @Test
    @DisplayName("The enters ability creates the delayed trigger even after its source leaves")
    void sourceCanLeaveBeforeEntersAbilityResolves() {
        addCreatureReady(player2, new FugitiveWizard());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        castMassacreGirl();
        harness.passBothPriorities();
        Permanent girl = findPermanent(player1, "Massacre Girl");

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, girl.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(girl.getCard());
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deaths before the enters ability resolves do not trigger the delayed ability")
    void deathsBeforeEntersAbilityResolvesDoNotTrigger() {
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        castMassacreGirl();
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, wizard.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(wizard.getCard());
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
    }

    @Test
    @DisplayName("An earlier delayed ability weakens Massacre Girl after she leaves and returns")
    void returnedMassacreGirlIsANewObjectForEarlierDelayedAbility() {
        castMassacreGirl();
        resolveAllTriggers();
        Permanent originalGirl = findPermanent(player1, "Massacre Girl");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, originalGirl.getId());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent returnedGirl = findPermanent(player1, "Massacre Girl");
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, wizard.getId());
        resolveAllTriggers();

        assertThat(returnedGirl.getId()).isNotEqualTo(originalGirl.getId());
        assertThat(gqs.getEffectivePower(gd, returnedGirl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedGirl)).isEqualTo(3);
    }

    @Test
    @DisplayName("The delayed death ability expires at end of turn")
    void delayedAbilityExpiresAtEndOfTurn() {
        castMassacreGirl();
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        Permanent giant = addCreatureReady(player2, new HillGiant());
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, wizard.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    private void castMassacreGirl() {
        harness.setHand(player1, List.of(new MassacreGirl()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
