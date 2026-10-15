package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.IronHillsStalwart;
import com.github.laxika.magicalvibes.cards.h.HellsparkElemental;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEaglesAreComing.class, IronHillsStalwart.class, HellsparkElemental.class})
class TheEaglesAreComingTest extends BaseCardTest {

    @Test
    void returnsOneOwnCreatureAndCreatesOneBirdSoldierAtNextUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        cast(target);

        harness.assertInHand(player1, "Iron Hills Stalwart");
        assertThat(findPermanents(player1, "Bird Soldier")).isEmpty();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        List<Permanent> birds = findPermanents(player1, "Bird Soldier");
        assertThat(birds).hasSize(1);
        assertThat(birds.getFirst().getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.BIRD, CardSubtype.SOLDIER);
        assertThat(gqs.hasKeyword(gd, birds.getFirst(), Keyword.FLYING)).isTrue();
    }

    @Test
    void kickedSpellReturnsAnyNumberOfOwnCreaturesAndCreatesOneBirdPerCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        prepareKickedSpell();
        castKickedTargets(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Iron Hills Stalwart")).isEmpty();
        harness.assertInHand(player1, "Iron Hills Stalwart");

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(2);
    }

    @Test
    void createsBirdsAtTheNextUpkeepEvenIfItIsTheOpponents() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        cast(target);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(1);
    }

    @Test
    void cannotTargetAnOpponentOwnedCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new IronHillsStalwart());
        harness.setHand(player1, List.of(new TheEaglesAreComing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own");
    }

    @Test
    void kickedSpellCanChooseNoCreatures() {
        prepareKickedSpell();
        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Eagles Are Coming!");
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird Soldier")).isEmpty();
    }

    @Test
    void canReturnAnOwnedCreatureControlledByTheOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronHillsStalwart());
        gd.stolenCreatures.put(target.getId(), player1.getId());

        cast(target);

        harness.assertInHand(player1, "Iron Hills Stalwart");
        harness.assertNotInHand(player2, "Iron Hills Stalwart");
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(1);
        assertThat(findPermanents(player2, "Bird Soldier")).isEmpty();
    }

    @Test
    void cannotReturnAnOpponentOwnedCreatureEvenIfYouControlIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new TheEaglesAreComing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own");
    }

    @Test
    void returningACreatureTokenStillCountsForTheDelayedBird() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        cast(creature);
        advanceToUpkeep(player2);
        resolveAllTriggers();
        Permanent bird = findPermanent(player1, "Bird Soldier");

        cast(bird);

        harness.assertNotOnBattlefield(player1, "Bird Soldier");
        harness.assertNotInHand(player1, "Bird Soldier");
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(1);
    }

    @Test
    void onlyCountsTargetsStillPresentAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        prepareKickedSpell();
        castKickedTargets(List.of(first.getId(), second.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, first));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Iron Hills Stalwart");
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(1);
    }

    @Test
    void doesNotCreateBirdWhenItsOnlyTargetHasLeftTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        harness.setHand(player1, List.of(new TheEaglesAreComing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird Soldier")).isEmpty();
    }

    @Test
    void createsBirdsOnlyOnce() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart());
        cast(target);
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(1);
    }

    @Test
    void doesNotCountACreatureExiledInsteadOfReturnedToHand() {
        harness.setGraveyard(player1, List.of(new HellsparkElemental()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent target = findPermanent(player1, "Hellspark Elemental");

        cast(target);

        harness.assertNotInHand(player1, "Hellspark Elemental");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird Soldier")).isEmpty();
    }

    @Test
    void kickedSpellCanReturnMoreThanNinetyNineCreatures() {
        List<UUID> targets = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new IronHillsStalwart()).getId())
                .toList();
        prepareKickedSpell();
        castKickedTargets(targets);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Iron Hills Stalwart");
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(100);
    }

    private void prepareKickedSpell() {
        harness.setHand(player1, List.of(new TheEaglesAreComing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castKickedTargets(List<UUID> targets) {
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, targets, List.of(), false,
                null, null, null, null, null, true);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new TheEaglesAreComing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
