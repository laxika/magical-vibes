package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InallaArchmageRitualist.class, FugitiveWizard.class, GrizzlyBears.class, Unsummon.class})
class InallaArchmageRitualistTest extends BaseCardTest {

    @Test
    void eminenceCopiesAWizardFromTheBattlefield() {
        harness.addToBattlefield(player1, new InallaArchmageRitualist());
        castCreatureWithOneMana(new FugitiveWizard());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(2);
        Permanent token = findPermanents(player1, "Fugitive Wizard").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
    }

    @Test
    void eminenceWorksFromTheCommandZone() {
        Card inalla = new InallaArchmageRitualist();
        gd.playerCommandZones.get(player1.getId()).add(inalla);
        castCreatureWithOneMana(new FugitiveWizard());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(2);
    }

    @Test
    void eminenceDoesNotTriggerForNonWizards() {
        harness.addToBattlefield(player1, new InallaArchmageRitualist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void activatedAbilityTapsFiveWizardsAndMakesTargetPlayerLoseSevenLife() {
        Permanent inalla = addCreatureReady(player1, new InallaArchmageRitualist());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new FugitiveWizard());
        }

        List<Permanent> wizards = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.WIZARD))
                .filter(permanent -> !permanent.isTapped())
                .limit(5)
                .toList();
        harness.setLife(player2, 20);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(inalla), 0,
                null, player2.getId());
        // With exactly five eligible Wizards, the engine pays the tap cost automatically.
        harness.passBothPriorities();

        assertThat(wizards).hasSize(5);
        assertThat(wizards).allMatch(Permanent::isTapped);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void eminenceCanBeDeclined() {
        harness.addToBattlefield(player1, new InallaArchmageRitualist());
        castCreatureWithOneMana(new FugitiveWizard());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    void eminenceDoesNotTriggerForAnOpponentsWizard() {
        harness.addToBattlefield(player1, new InallaArchmageRitualist());
        harness.enterBattlefieldAndReturn(player2, new FugitiveWizard());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void eminenceDoesNotTriggerForInallaItself() {
        harness.setHand(player1, List.of(new InallaArchmageRitualist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Inalla, Archmage Ritualist")).hasSize(1);
    }

    @Test
    void eminenceCopiesAWizardThatLeftBeforeResolution() {
        harness.addToBattlefield(player1, new InallaArchmageRitualist());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent wizard = harness.enterBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, wizard.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(1);
        assertThat(findPermanent(player1, "Fugitive Wizard").getCard().isToken()).isTrue();
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    void eminenceDoesNotOfferPaymentWhenInallaLeftBeforeResolution() {
        Permanent inalla = harness.addToBattlefieldAndReturn(player1, new InallaArchmageRitualist());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.enterBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, inalla.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(1);
        harness.assertInHand(player1, "Inalla, Archmage Ritualist");
    }

    @Test
    void activatedAbilityCanTapSummoningSickWizardsAndTargetItsController() {
        Permanent inalla = harness.addToBattlefieldAndReturn(player1, new InallaArchmageRitualist());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new FugitiveWizard());
        }
        List<Permanent> wizards = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        wizards.forEach(permanent -> permanent.setSummoningSick(true));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(inalla), 0,
                null, player1.getId());
        harness.passBothPriorities();

        assertThat(wizards).allMatch(Permanent::isTapped);
        harness.assertLife(player1, 13);
    }

    @Test
    void copiedWizardIsExiledAtTheNextEndStepWithoutTriggeringEminenceAgain() {
        harness.addToBattlefield(player1, new InallaArchmageRitualist());
        castCreatureWithOneMana(new FugitiveWizard());
        harness.handleMayAbilityChosen(player1, true);
        Permanent token = findPermanents(player1, "Fugitive Wizard").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(1);
        harness.assertNotInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    void tappedWizardsCannotPayTheActivatedAbilityCost() {
        Permanent inalla = harness.addToBattlefieldAndReturn(player1, new InallaArchmageRitualist());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new FugitiveWizard());
        }
        findPermanent(player1, "Fugitive Wizard").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(inalla), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
        assertThat(inalla.isTapped()).isFalse();
    }

    private void castCreatureWithOneMana(Card creature) {
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
