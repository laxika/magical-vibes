package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RangerClass.class, Forest.class, GrizzlyBears.class})
class RangerClassTest extends BaseCardTest {

    @Test
    void entersAndCreatesWolfToken() {
        harness.setHand(player1, List.of(new RangerClass()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().isToken()).isTrue();
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    void levelTwoPutsCounterOnTargetAttackingCreature() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        levelUpToTwo(rangerClass);

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void levelThreeAllowsCastingCreatureFromTopOfLibrary() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        levelUpToThree(rangerClass);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void levelThreeDoesNotAllowCastingNoncreatureFromTopOfLibrary() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        levelUpToThree(rangerClass);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(forest);
    }

    @Test
    void levelOneDoesNotTriggerWhenAttacking() {
        harness.addToBattlefield(player1, new RangerClass());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multipleAttackersReceiveOnlyOneCounterAndNonattackerIsNotEligible() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        levelUpToThree(rangerClass);

        declareAttackers(List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId())
                .doesNotContain(nonattacker.getId());
        harness.handlePermanentChosen(player1, first.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void levelThreePrivatelyShowsTopCardDuringOpponentsTurn() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        levelUpToThree(rangerClass);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player2);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void levelTwoDoesNotAllowCastingFromLibrary() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        levelUpToTwo(rangerClass);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void levelThreeDoesNotAllowCastingNoncreatureSpellFromLibrary() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        levelUpToThree(rangerClass);
        RangerClass topCard = new RangerClass();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void cannotSkipLevelTwoOrActivateLevelTwoAgain() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(rangerClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, battlefieldIndex(rangerClass), 0, null, null);
        harness.passBothPriorities();

        assertThat(rangerClass.getClassLevel()).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(rangerClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelAbilityCannotBeActivatedOutsideMainPhase() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(rangerClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rangerClass.getClassLevel()).isEqualTo(1);
    }

    @Test
    void libraryCastingStillRequiresManaAndNormalCreatureTiming() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        levelUpToThree(rangerClass);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void canCastSuccessiveCreaturesFromLibraryInSameTurn() {
        Permanent rangerClass = harness.addToBattlefieldAndReturn(player1, new RangerClass());
        levelUpToThree(rangerClass);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void levelUpToTwo(Permanent rangerClass) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, battlefieldIndex(rangerClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent rangerClass) {
        levelUpToTwo(rangerClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, battlefieldIndex(rangerClass), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
