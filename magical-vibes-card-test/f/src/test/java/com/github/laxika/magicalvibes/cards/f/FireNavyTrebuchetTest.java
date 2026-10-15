package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNavyTrebuchet.class, GrizzlyBears.class, RayOfCommand.class})
@DisplayName("Fire Navy Trebuchet")
class FireNavyTrebuchetTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking Ballistic Boulder")
    void attackingCreatesBallisticBoulder() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveTokenAttackTargetChoice();

        Permanent boulder = findPermanent(player1, "Ballistic Boulder");
        assertThat(boulder.isTapped()).isTrue();
        assertThat(boulder.isAttacking()).isTrue();
        assertThat(boulder.isAttackedThisTurn()).isFalse();
        assertThat(boulder.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(boulder.getCard().getPower()).isEqualTo(2);
        assertThat(boulder.getCard().getToughness()).isEqualTo(1);
        assertThat(boulder.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(boulder.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("The Ballistic Boulder is sacrificed at the beginning of the next end step")
    void ballisticBoulderIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveTokenAttackTargetChoice();
        assertThat(findPermanent(player1, "Ballistic Boulder")).isNotNull();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Ballistic Boulder")).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ballistic Boulder")).isEmpty();
    }

    @Test
    @DisplayName("Multiple attackers create only one Boulder per Trebuchet")
    void multipleAttackersCreateOnlyOneBoulder() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveTokenAttackTargetChoice();

        assertThat(findPermanents(player1, "Ballistic Boulder")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent attacking does not create a Boulder")
    void opponentAttackingDoesNotTrigger() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Ballistic Boulder")).isEmpty();
        assertThat(findPermanents(player2, "Ballistic Boulder")).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger resolves after the Trebuchet leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent trebuchet = addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(trebuchet);
        gd.playerGraveyards.get(player1.getId()).add(trebuchet.getCard());
        resolveTokenAttackTargetChoice();

        assertThat(findPermanents(player1, "Ballistic Boulder")).hasSize(1);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Ballistic Boulder")).isEmpty();
    }

    @Test
    @DisplayName("A stolen Boulder cannot be sacrificed by the original ability controller")
    void stolenBoulderSurvivesDelayedSacrifice() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(1));
        resolveTokenAttackTargetChoice();
        Permanent boulder = findPermanent(player1, "Ballistic Boulder");

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, boulder.getId());
        assertThat(findPermanents(player2, "Ballistic Boulder")).containsExactly(boulder);

        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(findPermanents(player2, "Ballistic Boulder")).containsExactly(boulder);
    }

    private void resolveTokenAttackTargetChoice() {
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
    }
}
