package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfIndemnity.class, ZulaportCutthroat.class, InfernalGrasp.class})
class AngelOfIndemnityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target permanent card with mana value 4 or less")
    void etbReturnsPermanentFromGraveyard() {
        ZulaportCutthroat target = new ZulaportCutthroat();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new AngelOfIndemnity()));
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zulaport Cutthroat");
        harness.assertNotInGraveyard(player1, "Zulaport Cutthroat");
    }

    @Test
    @DisplayName("ETB does not offer a permanent card with mana value above 4")
    void etbRejectsHighManaValuePermanent() {
        harness.setGraveyard(player1, List.of(new AngelOfIndemnity()));
        harness.setHand(player1, List.of(new AngelOfIndemnity()));
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Angel of Indemnity");
    }

    @Test
    @DisplayName("Encore creates an untapped hasty copy that is not already attacking")
    void encoreCreatesUntappedHastyCopy() {
        createEncoreCopy();

        Permanent token = findPermanent(player1, "Angel of Indemnity");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
    }

    private void createEncoreCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new AngelOfIndemnity()));
        addEncoreMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.assertNotInGraveyard(player1, "Angel of Indemnity");
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    @Test
    @DisplayName("Encore sacrifices its token at the next end step")
    void encoreSacrificesTokenAtNextEndStep() {
        createEncoreCopy();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("ETB cannot return an instant or an opponent's permanent")
    void etbRejectsNonPermanentAndOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new InfernalGrasp()));
        harness.setGraveyard(player2, List.of(new ZulaportCutthroat()));
        harness.setHand(player1, List.of(new AngelOfIndemnity()));
        addCastingMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Infernal Grasp");
        harness.assertInGraveyard(player2, "Zulaport Cutthroat");
    }

    @Test
    @DisplayName("Encore copy triggers the graveyard return ability")
    void encoreCopyReturnsPermanent() {
        ZulaportCutthroat target = new ZulaportCutthroat();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new AngelOfIndemnity(), target));
        addEncoreMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Zulaport Cutthroat");
        harness.assertNotInGraveyard(player1, "Zulaport Cutthroat");
    }

    @Test
    @DisplayName("Encore cannot be activated during combat")
    void encoreRequiresSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setGraveyard(player1, List.of(new AngelOfIndemnity()));
        addEncoreMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Angel of Indemnity");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not return a target that leaves the graveyard before resolution")
    void etbDoesNotReturnMissingTarget() {
        ZulaportCutthroat target = new ZulaportCutthroat();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new AngelOfIndemnity()));
        addCastingMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Zulaport Cutthroat");
        harness.assertOnBattlefield(player1, "Angel of Indemnity");
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void addEncoreMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
