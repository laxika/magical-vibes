package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SylvanShepherd;
import com.github.laxika.magicalvibes.model.Card;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VhalEagerScholar.class, GrizzlyBears.class, Island.class, Forest.class, Mountain.class, Plains.class, Swamp.class, SylvanShepherd.class, Ornithopter.class})
class VhalEagerScholarTest extends BaseCardTest {

    @Test
    void lootingPutsStudyCounterOnVhal() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(vhal.getCounterCount(CounterType.STUDY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void blueSpecializationRemovesStudyCountersAndLooksAtThatManyCards() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        vhal.setCounterCount(CounterType.STUDY, 2);
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(vhal.getCard().getName()).isEqualTo("Vhal, Scholar of Prophecy");
        assertThat(vhal.getCounterCount(CounterType.STUDY)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(com.github.laxika.magicalvibes.model.PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    void studyCountersRemainUntilSpecializationTriggerResolves() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        vhal.setCounterCount(CounterType.STUDY, 2);
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new SylvanShepherd(), new Forest()));

        activateSpecialization(2);
        harness.passBothPriorities();

        assertThat(vhal.getCounterCount(CounterType.STUDY)).isEqualTo(2);
        assertThat(gd.stack).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void specializedFacesLootWithoutAddingStudyCounters(int abilityIndex) {
        Card discarded = switch (abilityIndex) {
            case 1 -> new Plains();
            case 2 -> new Island();
            case 3 -> new Swamp();
            case 4 -> new Mountain();
            case 5 -> new Forest();
            default -> throw new IllegalArgumentException();
        };
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new SylvanShepherd()));
        activateSpecialization(abilityIndex);
        resolveAllTriggers();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(vhal.getCounterCount(CounterType.STUDY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void blueFacePutsUnchosenCardsOnBottomInRandomOrderWithoutAnotherChoice() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        vhal.setCounterCount(CounterType.STUDY, 3);
        SylvanShepherd chosen = new SylvanShepherd();
        Forest second = new Forest();
        Mountain third = new Mountain();
        Island untouched = new Island();
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(chosen, second, third, untouched));

        activateSpecialization(2);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(second, third);
    }

    @Test
    void blackFaceReturnsOpponentsEligibleCreatureUnderYourControl() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        vhal.setCounterCount(CounterType.STUDY, 3);
        SylvanShepherd creature = new SylvanShepherd();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new Swamp()));

        activateSpecialization(3);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sylvan Shepherd");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(creature);
        assertThat(vhal.getCounterCount(CounterType.STUDY)).isZero();
    }

    @Test
    void redFaceDealsDamageEqualToRemovedCountersToOpponentsCreature() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        vhal.setCounterCount(CounterType.STUDY, 2);
        Permanent target = addCreatureReady(player2, new SylvanShepherd());
        harness.setHand(player1, List.of(new Mountain()));

        activateSpecialization(4);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(vhal.getCounterCount(CounterType.STUDY)).isZero();
    }

    @Test
    void whiteFaceDistributesRemovedStudyCountersToChosenCreature() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        vhal.setCounterCount(CounterType.STUDY, 2);
        Permanent target = addCreatureReady(player2, new SylvanShepherd());
        harness.setHand(player1, List.of(new Plains()));

        activateSpecialization(1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "2");
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(vhal.getCounterCount(CounterType.STUDY)).isZero();
    }

    @Test
    void greenFaceSeeksTwoEligibleCreaturesAndReturnsUnchosenCardToLibrary() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        vhal.setCounterCount(CounterType.STUDY, 3);
        SylvanShepherd chosen = new SylvanShepherd();
        SylvanShepherd other = new SylvanShepherd();
        Island land = new Island();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(chosen, other, land));

        activateSpecialization(5);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(chosen.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(other, land);
        assertThat(vhal.getCounterCount(CounterType.STUDY)).isZero();
    }

    @Test
    void enteringBattlefieldAsSpecializedFaceDoesNotTriggerSpecializationAbility() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        harness.setHand(player1, List.of(new Mountain()));
        activateSpecialization(4);
        resolveAllTriggers();
        Card specialized = vhal.getCard();
        gd.playerBattlefields.get(player1.getId()).clear();
        addCreatureReady(player2, new SylvanShepherd());

        harness.enterBattlefieldAndReturn(player1, specialized);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void greenFaceCanSeekZeroManaCreaturesWithoutStudyCounters() {
        addCreatureReady(player1, new VhalEagerScholar());
        Ornithopter chosen = new Ornithopter();
        Ornithopter other = new Ornithopter();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(chosen, other, new SylvanShepherd()));

        activateSpecialization(5);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(com.github.laxika.magicalvibes.model.PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(chosen.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).contains(other);
    }
    private void activateSpecialization(int abilityIndex) {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
    }
}
