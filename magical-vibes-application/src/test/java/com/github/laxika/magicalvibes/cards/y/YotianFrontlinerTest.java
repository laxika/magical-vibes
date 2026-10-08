package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
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

@CardUsed({YotianFrontliner.class, Disfigure.class})
class YotianFrontlinerTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another creature I control")
    void attackTriggerRestrictsTargets() {
        Permanent frontliner = addCreatureReady(player1, new YotianFrontliner());
        Permanent ownCreature = addCreatureReady(player1, new YotianFrontliner());
        Permanent opponentCreature = addCreatureReady(player2, new YotianFrontliner());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(frontliner.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("Attack trigger gives another creature +1/+1 until end of turn")
    void attackTriggerBoostsTarget() {
        addCreatureReady(player1, new YotianFrontliner());
        Permanent ownCreature = addCreatureReady(player1, new YotianFrontliner());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Unearth returns Yotian Frontliner with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new YotianFrontliner()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent frontliner = findPermanent(player1, "Yotian Frontliner");
        assertThat(frontliner.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Yotian Frontliner");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Yotian Frontliner"));
    }

    @Test
    void attackBoostExpiresAtCleanup() {
        addCreatureReady(player1, new YotianFrontliner());
        Permanent target = addCreatureReady(player1, new YotianFrontliner());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void attackAloneDoesNotRequireAnIllegalTarget() {
        Permanent frontliner = addCreatureReady(player1, new YotianFrontliner());
        addCreatureReady(player2, new YotianFrontliner());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(frontliner.getPowerModifier()).isZero();
        assertThat(frontliner.getToughnessModifier()).isZero();
    }

    @Test
    void attackTriggerResolvesAfterItsSourceLeaves() {
        Permanent frontliner = addCreatureReady(player1, new YotianFrontliner());
        Permanent target = addCreatureReady(player1, new YotianFrontliner());
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, frontliner.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(frontliner);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void unearthedCreatureIsExiledInsteadOfDying() {
        YotianFrontliner card = new YotianFrontliner();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent frontliner = findPermanent(player1, "Yotian Frontliner");
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, frontliner.getId());

        harness.assertNotOnBattlefield(player1, "Yotian Frontliner");
        harness.assertNotInGraveyard(player1, "Yotian Frontliner");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void unearthedCreatureCanAttackImmediatelyAndTriggerItsAbility() {
        Permanent target = addCreatureReady(player1, new YotianFrontliner());
        harness.setGraveyard(player1, List.of(new YotianFrontliner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void attackTriggerDoesNotBoostAnotherCreatureWhenItsTargetLeaves() {
        Permanent frontliner = addCreatureReady(player1, new YotianFrontliner());
        Permanent target = addCreatureReady(player1, new YotianFrontliner());
        Permanent otherCreature = addCreatureReady(player1, new YotianFrontliner());
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        resolveAllTriggers();

        assertThat(frontliner.getPowerModifier()).isZero();
        assertThat(frontliner.getToughnessModifier()).isZero();
        assertThat(otherCreature.getPowerModifier()).isZero();
        assertThat(otherCreature.getToughnessModifier()).isZero();
    }

    @Test
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new YotianFrontliner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Yotian Frontliner");
    }

    @Test
    void unearthCannotBeActivatedOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new YotianFrontliner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Yotian Frontliner");
    }
}
