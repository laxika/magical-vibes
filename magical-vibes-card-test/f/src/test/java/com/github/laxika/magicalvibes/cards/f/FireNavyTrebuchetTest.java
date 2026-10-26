package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.t.TeferiWhoSlowsTheSunset;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({FireNavyTrebuchet.class, GrizzlyBears.class, RayOfCommand.class, QueenAllenalOfRuadach.class,
        ParallelLives.class, TeferiWhoSlowsTheSunset.class})
@DisplayName("Fire Navy Trebuchet")
class FireNavyTrebuchetTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking Ballistic Boulder")
    void attackingCreatesBallisticBoulder() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveBoulderTrigger();

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
        resolveBoulderTrigger();
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
        resolveBoulderTrigger();

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
        resolveBoulderTrigger();

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
        resolveBoulderTrigger();
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

    @Test
    @DisplayName("The attacking player orders Queen Allenal and Parallel Lives before the Boulder is created")
    void asksToOrderTokenReplacements() {
        attackWithQueenAndParallelLives();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(findPermanents(player1, "Ballistic Boulder")).isEmpty();
    }

    @Test
    @DisplayName("Applying Queen Allenal first doubles the added Soldier, and every token enters tapped and attacking")
    void queenFirstDoublesTheAddedSoldierAndEveryTokenAttacks() {
        attackWithQueenAndParallelLives();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> chooseReplacement("Queen Allenal of Ruadach"));

        List<Permanent> boulders = findPermanents(player1, "Ballistic Boulder");
        List<Permanent> soldiers = tokensNamed("Soldier");
        assertThat(boulders).hasSize(2);
        assertThat(soldiers).hasSize(2);
        assertThat(java.util.stream.Stream.concat(boulders.stream(), soldiers.stream())).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("Applying Parallel Lives first adds one undoubled Soldier")
    void parallelLivesFirstAddsOneSoldier() {
        attackWithQueenAndParallelLives();

        chooseReplacement("Parallel Lives");

        assertThat(findPermanents(player1, "Ballistic Boulder")).hasSize(2);
        assertThat(tokensNamed("Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Every token of the ordered creation, the added Soldiers included, is sacrificed at the next end step")
    void everyTokenOfTheOrderedCreationIsSacrificed() {
        attackWithQueenAndParallelLives();
        chooseReplacement("Queen Allenal of Ruadach");

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ballistic Boulder")).isEmpty();
        assertThat(tokensNamed("Soldier")).isEmpty();
    }

    @Test
    @DisplayName("After the replacements are ordered, each creature token chooses its own player or planeswalker")
    void eachTokenChoosesItsAttackTargetAfterTheOrder() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TeferiWhoSlowsTheSunset());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        attackWithQueenAndParallelLives();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            chooseReplacement("Parallel Lives");
            harness.handlePermanentChosen(player1, planeswalker.getId());
            harness.handlePermanentChosen(player1, player2.getId());
            harness.handlePermanentChosen(player1, player2.getId());
        });

        List<Permanent> boulders = findPermanents(player1, "Ballistic Boulder");
        assertThat(boulders).extracting(Permanent::getAttackTarget)
                .containsExactly(planeswalker.getId(), player2.getId());
        assertThat(tokensNamed("Soldier")).singleElement().satisfies(soldier -> {
            assertThat(soldier.isAttacking()).isTrue();
            assertThat(soldier.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    private void attackWithQueenAndParallelLives() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());

        declareAttackers(List.of(1));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveBoulderTrigger);
    }

    private void chooseReplacement(String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow());
    }

    private List<Permanent> tokensNamed(String name) {
        return findPermanents(player1, name).stream().filter(permanent -> permanent.getCard().isToken()).toList();
    }

    private void resolveBoulderTrigger() {
        harness.passBothPriorities();
    }
}
