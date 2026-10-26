package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.cards.u.UginTheSpiritDragon;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedoubledStormsinger.class, GrizzlyBears.class, RoleReversal.class, ParallelLives.class,
        QueenAllenalOfRuadach.class, UginTheSpiritDragon.class})
class RedoubledStormsingerTest extends BaseCardTest {

    @Test
    @DisplayName("Copies creature tokens that entered this turn tapped and attacking")
    void copiesEnteredTokensTappedAndAttacking() {
        Permanent stormsinger = addCreatureReady(player1, new RedoubledStormsinger());
        Permanent originalToken = addEnteredCreatureToken(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        List<Permanent> tokens = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        Permanent copy = tokens.stream()
                .filter(permanent -> permanent != originalToken)
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(stormsinger.isAttacking()).isTrue();

        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(originalToken);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
    }

    @Test
    @DisplayName("Does not copy a creature token that did not enter this turn")
    void ignoresOlderToken() {
        addCreatureReady(player1, new RedoubledStormsinger());
        Card olderToken = new GrizzlyBears();
        olderToken.setToken(true);
        addCreatureReady(player1, olderToken);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Copies every eligible token without copying the newly created copies again")
    void copiesEachEligibleTokenOnce() {
        addCreatureReady(player1, new RedoubledStormsinger());
        Permanent first = addEnteredCreatureToken(player1);
        Permanent second = addEnteredCreatureToken(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        List<Permanent> copies = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent != first && permanent != second)
                .toList();
        assertThat(copies).hasSize(2).allSatisfy(copy -> {
            assertThat(copy.isTapped()).isTrue();
            assertThat(copy.isAttacking()).isTrue();
            assertThat(copy.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(first, second);
    }

    @Test
    @DisplayName("Checks token eligibility when the attack trigger resolves")
    void includesTokenEnteredAfterAttackAndIgnoresOpponentsTokensAndNontokens() {
        addCreatureReady(player1, new RedoubledStormsinger());
        addCreatureReady(player2, new GrizzlyBears());
        addEnteredCreatureToken(player2);
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        Permanent lateToken = addEnteredCreatureToken(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(3);
        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2).contains(lateToken);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @CardUsed({RedoubledStormsinger.class, GrizzlyBears.class, RoleReversal.class})
    @DisplayName("The ability controller cannot sacrifice a copy now controlled by an opponent")
    void doesNotSacrificeCopyAfterControlExchange() {
        addCreatureReady(player1, new RedoubledStormsinger());
        Permanent original = addEnteredCreatureToken(player1);
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        Permanent copy = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent != original).findFirst().orElseThrow();

        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RoleReversal()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(copy.getId(), opposingCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(copy);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(copy);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(original);
    }

    @Test
    @DisplayName("Each copy chooses its own player or planeswalker to attack")
    void eachCopyChoosesItsAttackTarget() {
        addCreatureReady(player1, new RedoubledStormsinger());
        addEnteredCreatureToken(player1);
        addEnteredCreatureToken(player1);
        addCreatureReady(player2, new GrizzlyBears());
        Permanent ugin = harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon());
        ugin.setCounterCount(CounterType.LOYALTY, 7);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, ugin.getId());

        List<Permanent> copies = bearTokens().stream().filter(Permanent::isAttacking).toList();
        assertThat(copies).hasSize(2).allSatisfy(copy -> assertThat(copy.isTapped()).isTrue());
        assertThat(copies).extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(player2.getId(), ugin.getId());
    }

    @Test
    @DisplayName("Each copy Parallel Lives doubles chooses its own attack target")
    void doubledCopiesChooseTheirAttackTargetsIndependently() {
        addCreatureReady(player1, new RedoubledStormsinger());
        addEnteredCreatureToken(player1);
        harness.addToBattlefield(player1, new ParallelLives());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent ugin = harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon());
        ugin.setCounterCount(CounterType.LOYALTY, 7);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, ugin.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());

        List<Permanent> copies = bearTokens().stream().filter(Permanent::isAttacking).toList();
        assertThat(copies).hasSize(2).allSatisfy(copy -> assertThat(copy.isTapped()).isTrue());
        assertThat(copies).extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(player2.getId(), ugin.getId());
    }

    @Test
    @DisplayName("With Queen Allenal and Parallel Lives, the controller orders them before the copies enter")
    void asksToOrderTokenReplacements() {
        attackUnderQueenAndParallelLives();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(bearTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Applying Queen Allenal before Parallel Lives doubles the copy and the Soldier, all attacking")
    void queenFirstDoublesCopyAndSoldier() {
        Permanent original = attackUnderQueenAndParallelLives();

        chooseReplacement("Queen Allenal of Ruadach");

        assertThat(bearTokens().stream().filter(token -> token != original)).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(token -> {
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("Applying Parallel Lives before Queen Allenal adds one undoubled Soldier")
    void parallelLivesFirstAddsOneSoldier() {
        Permanent original = attackUnderQueenAndParallelLives();

        chooseReplacement("Parallel Lives");

        assertThat(bearTokens().stream().filter(token -> token != original)).hasSize(2);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    private Permanent attackUnderQueenAndParallelLives() {
        addCreatureReady(player1, new RedoubledStormsinger());
        Permanent original = addEnteredCreatureToken(player1);
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        return original;
    }

    private List<Permanent> bearTokens() {
        return findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
    }

    private void chooseReplacement(String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow());
    }

    private Permanent addEnteredCreatureToken(Player player) {
        Card token = new GrizzlyBears();
        token.setToken(true);
        Permanent permanent = harness.enterBattlefieldAndReturn(player, token);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
