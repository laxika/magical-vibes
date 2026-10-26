package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ElspethSunsChampion;
import com.github.laxika.magicalvibes.cards.n.NestingDovehawk;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorStyleMaster.class, GrizzlyBears.class, NestingDovehawk.class, ElspethSunsChampion.class,
        QueenAllenalOfRuadach.class, ParallelLives.class})
class MirrorStyleMasterTest extends BaseCardTest {

    @Test
    @DisplayName("With Queen Allenal and Parallel Lives, the attacking copy's creator orders them first")
    void asksToOrderTokenReplacementsForAttackingCopy() {
        attackWithModifiedBearsUnderQueenAndParallelLives(null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(bearTokens()).isEmpty();
    }

    @Test
    @DisplayName("Applying Queen Allenal before Parallel Lives doubles the attacking copy and the added Soldier")
    void queenFirstDoublesAttackingCopyAndSoldier() {
        attackWithModifiedBearsUnderQueenAndParallelLives(null);

        chooseReplacement("Queen Allenal of Ruadach");

        assertThat(bearTokens()).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    @DisplayName("Applying Parallel Lives before Queen Allenal adds one undoubled Soldier")
    void parallelLivesFirstAddsOneSoldier() {
        attackWithModifiedBearsUnderQueenAndParallelLives(null);

        chooseReplacement("Parallel Lives");

        assertThat(bearTokens()).hasSize(2);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Each doubled copy chooses its own player or planeswalker to attack")
    void doubledCopiesChooseTheirAttackTargetsIndependently() {
        Permanent elspeth = new Permanent(new ElspethSunsChampion());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);
        attackWithModifiedBearsUnderQueenAndParallelLives(elspeth);

        chooseReplacement("Parallel Lives");
        chooseAttackTarget(player2.getId());
        chooseAttackTarget(elspeth.getId());
        chooseAttackTarget(player2.getId());

        assertThat(bearTokens()).hasSize(2).allSatisfy(token -> assertThat(token.isAttacking()).isTrue());
        assertThat(bearTokens()).extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(player2.getId(), elspeth.getId());
        assertThat(findPermanents(player1, "Soldier")).singleElement()
                .satisfies(soldier -> assertThat(soldier.getAttackTarget()).isEqualTo(player2.getId()));
    }

    @Test
    @DisplayName("Backup grants the attack trigger to another creature")
    void backupGrantsAttackTriggerToAnotherCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castMirrorStyleMaster(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Copies each attacking modified creature you control")
    void copiesEachAttackingModifiedCreatureYouControl() {
        Permanent master = addCreatureReady(player1, new MirrorStyleMaster());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodified = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingModified = addCreatureReady(player2, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingModified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(master),
                gd.playerBattlefields.get(player1.getId()).indexOf(modified),
                gd.playerBattlefields.get(player1.getId()).indexOf(unmodified)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Copies are exiled at end of combat")
    void copiesAreExiledAtEndOfCombat() {
        Permanent master = addCreatureReady(player1, new MirrorStyleMaster());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(master),
                gd.playerBattlefields.get(player1.getId()).indexOf(modified)));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    @DisplayName("Token copies enter together and see each other's entry")
    void tokenCopiesEnterSimultaneously() {
        addCreatureReady(player1, new MirrorStyleMaster());
        Permanent first = addCreatureReady(player1, new NestingDovehawk());
        Permanent second = addCreatureReady(player1, new NestingDovehawk());
        addCreatureReady(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Nesting Dovehawk"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token ->
                        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
    }

    @Test
    @DisplayName("The controller chooses what each attacking token attacks")
    void tokenAttackTargetCanDifferFromOriginal() {
        addCreatureReady(player1, new MirrorStyleMaster());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent elspeth = new Permanent(new ElspethSunsChampion());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Copies enter tapped and attacking without copying counters")
    void copiesDoNotInheritModifications() {
        addCreatureReady(player1, new MirrorStyleMaster());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1)
                .allSatisfy(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                });
    }

    private void attackWithModifiedBearsUnderQueenAndParallelLives(Permanent attackedPlaneswalker) {
        Permanent master = addCreatureReady(player1, new MirrorStyleMaster());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());
        addCreatureReady(player2, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int masterIndex = battlefield.indexOf(master);
        int modifiedIndex = battlefield.indexOf(modified);
        Map<Integer, UUID> attackTargets = attackedPlaneswalker == null
                ? Map.of(masterIndex, player2.getId(), modifiedIndex, player2.getId())
                : Map.of(masterIndex, player2.getId(), modifiedIndex, attackedPlaneswalker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(masterIndex, modifiedIndex), attackTargets);
            resolveAllTriggers();
        });
    }

    private List<Permanent> bearTokens() {
        return findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
    }

    private void chooseReplacement(String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> harness.handleListChoice(player1, choice.options()
                .stream().filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow()));
    }

    private void chooseAttackTarget(UUID targetId) {
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> harness.handlePermanentChosen(player1, targetId));
    }

    private void castMirrorStyleMaster(Permanent target) {
        harness.setHand(player1, List.of(new MirrorStyleMaster()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
