package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.a.AncestralKatana;
import com.github.laxika.magicalvibes.cards.e.EcologistsTerrarium;
import com.github.laxika.magicalvibes.cards.f.FavorOfJukai;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronApprentice;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnforgivingOne.class, FyndhornElves.class, GrizzlyBears.class,
        AncestralKatana.class, EcologistsTerrarium.class, FavorOfJukai.class,
        IronApprentice.class, JukaiTrainee.class, Ornithopter.class})
class UnforgivingOneTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns a target creature card within the number of modified creatures")
    void attackingReturnsCreatureWithinModifiedCreatureCount() {
        addCreatureReady(player1, new UnforgivingOne());
        Permanent modifiedCreature = addCreatureReady(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Card eligible = new FyndhornElves();
        Card tooExpensive = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(tooExpensive.getId());
    }

    @Test
    @DisplayName("An unmodified board provides no legal attack target")
    void noModifiedCreaturesMeansNoLegalTarget() {
        addCreatureReady(player1, new UnforgivingOne());
        Card eligibleWithoutModification = new FyndhornElves();
        harness.setGraveyard(player1, List.of(eligibleWithoutModification));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(eligibleWithoutModification);
    }

    @Test
    @DisplayName("A target becomes illegal if the modified-creature count decreases before resolution")
    void targetBecomesIllegalWhenModifiedCreatureCountDecreases() {
        addCreatureReady(player1, new UnforgivingOne());
        Permanent modifiedCreature = addCreatureReady(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card eligible = new FyndhornElves();
        harness.setGraveyard(player1, List.of(eligible));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
    }

    @Test
    void modifiedAttackerCountsItselfOnlyOnceDespiteMultipleModifications() {
        Permanent attacker = addCreatureReady(player1, new UnforgivingOne());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FavorOfJukai());
        aura.setAttachedTo(attacker.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AncestralKatana());
        equipment.setAttachedTo(attacker.getId());
        Card eligible = new IronApprentice();
        Card tooExpensive = new JukaiTrainee();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iron Apprentice");
        harness.assertInGraveyard(player1, "Jukai Trainee");
        Permanent returned = findPermanent(player1, "Iron Apprentice");
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isAttacking()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void ownAuraMakesAttackerModifiedWithoutCounters() {
        Permanent attacker = addCreatureReady(player1, new UnforgivingOne());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FavorOfJukai());
        aura.setAttachedTo(attacker.getId());
        Card target = new IronApprentice();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iron Apprentice");
    }

    @Test
    void opponentsAuraDoesNotMakeAttackerModified() {
        Permanent attacker = addCreatureReady(player1, new UnforgivingOne());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new FavorOfJukai());
        aura.setAttachedTo(attacker.getId());
        Card target = new IronApprentice();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Iron Apprentice");
    }

    @Test
    void opponentsEquipmentStillMakesAttackerModified() {
        Permanent attacker = addCreatureReady(player1, new UnforgivingOne());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AncestralKatana());
        equipment.setAttachedTo(attacker.getId());
        Card target = new IronApprentice();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iron Apprentice");
    }

    @Test
    void opponentsModifiedCreaturesAndOwnNoncreaturesDoNotIncreaseCount() {
        addCreatureReady(player1, new UnforgivingOne());
        Permanent opponent = addCreatureReady(player2, new JukaiTrainee());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EcologistsTerrarium());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card target = new IronApprentice();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Iron Apprentice");
    }

    @Test
    void zeroModifiedCreaturesCanReturnZeroManaValueCreature() {
        addCreatureReady(player1, new UnforgivingOne());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    void targetSelectionExcludesNoncreaturesAndOpponentsGraveyard() {
        Permanent attacker = addCreatureReady(player1, new UnforgivingOne());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card target = new IronApprentice();
        Card noncreature = new AncestralKatana();
        Card opponentsCard = new IronApprentice();
        harness.setGraveyard(player1, List.of(target, noncreature));
        harness.setGraveyard(player2, List.of(opponentsCard));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iron Apprentice");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    void triggerStillResolvesAfterUnmodifiedSourceLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new UnforgivingOne());
        Permanent modified = addCreatureReady(player1, new JukaiTrainee());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card target = new IronApprentice();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iron Apprentice");
        harness.assertInGraveyard(player1, "Unforgiving One");
    }

    @Test
    void twoModifiedCreaturesAllowTwoManaValueTarget() {
        Permanent attacker = addCreatureReady(player1, new UnforgivingOne());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent other = addCreatureReady(player1, new JukaiTrainee());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card target = new JukaiTrainee();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Jukai Trainee")).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Jukai Trainee");
    }

    @Test
    void targetThatLeavesGraveyardIsNotReturned() {
        Permanent attacker = addCreatureReady(player1, new UnforgivingOne());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card target = new IronApprentice();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.setHand(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Iron Apprentice");
        harness.assertInHand(player1, "Iron Apprentice");
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new UnforgivingOne());
        addCreatureReady(player2, new JukaiTrainee());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new UnforgivingOne());
        Permanent first = addCreatureReady(player2, new JukaiTrainee());
        Permanent second = addCreatureReady(player2, new JukaiTrainee());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
