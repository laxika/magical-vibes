package com.github.laxika.magicalvibes.cards.s;

import java.util.List;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderManToTheRescue.class, GrizzlyBears.class})
class SpiderManToTheRescueTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may tap Spider-Man to grant another nonattacking creature indestructible")
    void etbProtectsAnotherCreatureUntilEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent spiderMan = castSpiderMan();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(spiderMan.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The reflexive target must be another nonattacking creature you control")
    void targetSelectionUsesAnotherNonattackingCreatureYouControl() {
        Permanent attackingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent eligibleCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        Permanent spiderMan = castSpiderMan();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(eligibleCreature.getId())
                .doesNotContain(attackingCreature.getId(), opponentCreature.getId(), spiderMan.getId());

        harness.handlePermanentChosen(player1, eligibleCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, eligibleCreature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB tap leaves Spider-Man untapped and grants no protection")
    void decliningTapDoesNothing() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent spiderMan = castSpiderMan();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(spiderMan.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent castSpiderMan() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SpiderManToTheRescue()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Spider-Man, To the Rescue");
    }
}
