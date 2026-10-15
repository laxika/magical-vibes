package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfPower.class, GrizzlyBears.class, Shock.class})
class CircleOfPowerTest extends BaseCardTest {

    @Test
    void drawsLosesLifeCreatesWizardAndBoostsWizards() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CircleOfPower()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCircleOfPowerMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent wizard = findPermanent(player1, "Wizard");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(wizard.getCard().getSubtypes()).contains(CardSubtype.WIZARD);
        assertThat(wizard.getCard().getPower()).isZero();
        assertThat(wizard.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void wizardDamagesEachOpponentForNoncreatureSpell() {
        harness.setHand(player1, List.of(new CircleOfPower()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCircleOfPowerMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void wizardTriggerDoesNotFireForCreatureSpellAndBoostWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new CircleOfPower()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCircleOfPowerMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent wizard = findPermanent(player1, "Wizard");

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wizard)).isZero();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void tokenDamageGainsLifeBeforeTheNoncreatureSpellResolves() {
        harness.setHand(player1, List.of(new CircleOfPower()));
        harness.setLibrary(player1, List.of(new CircleOfPower(), new CircleOfPower()));
        addCircleOfPowerMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentNoncreatureSpellDoesNotTriggerWizard() {
        harness.setHand(player1, List.of(new CircleOfPower()));
        harness.setLibrary(player1, List.of(new CircleOfPower(), new CircleOfPower()));
        addCircleOfPowerMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondResolutionBoostsExistingWizardAndNewWizardOnlyOnceEach() {
        harness.setHand(player1, List.of(new CircleOfPower()));
        harness.setLibrary(player1, List.of(new CircleOfPower(), new CircleOfPower(),
                new CircleOfPower(), new CircleOfPower()));
        addCircleOfPowerMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent firstWizard = findPermanent(player1, "Wizard");

        addCircleOfPowerMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> wizards = gd.playerBattlefields.get(player1.getId());
        assertThat(wizards).hasSize(2);
        Permanent secondWizard = wizards.stream()
                .filter(permanent -> !permanent.getId().equals(firstWizard.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, firstWizard)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondWizard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, firstWizard, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondWizard, Keyword.LIFELINK)).isTrue();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 19);
    }

    @Test
    void tokenKeepsItsTriggerAfterTemporaryLifelinkExpires() {
        harness.setHand(player1, List.of(new CircleOfPower()));
        harness.setLibrary(player1, List.of(new CircleOfPower(), new CircleOfPower()));
        addCircleOfPowerMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    private void addCircleOfPowerMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
