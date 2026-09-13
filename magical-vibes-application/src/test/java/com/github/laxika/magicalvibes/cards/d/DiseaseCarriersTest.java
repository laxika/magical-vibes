package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.c.CinderSeer;
import com.github.laxika.magicalvibes.cards.g.GoblinBerserker;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.cards.s.ScentOfCinder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiseaseCarriers.class, HulkingOgre.class, GoblinBerserker.class,
        BraidwoodCup.class, ScentOfCinder.class, CinderSeer.class})
class DiseaseCarriersTest extends BaseCardTest {

    @Test
    @DisplayName("When Disease Carriers dies, target creature gets -2/-2")
    void deathTriggerGivesMinusTwoMinusTwo() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());

        destroyDiseaseCarriers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The death trigger can target a creature controlled by either player")
    void deathTriggerCanTargetAnyCreature() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HulkingOgre());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());

        destroyDiseaseCarriers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The death trigger -2/-2 kills a 2/2 creature")
    void deathTriggerKillsTwoTwoCreature() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        harness.addToBattlefield(player2, new GoblinBerserker());
        UUID targetId = harness.getPermanentId(player2, "Goblin Berserker");

        destroyDiseaseCarriers();

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Berserker");
        harness.assertInGraveyard(player2, "Goblin Berserker");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());

        destroyDiseaseCarriers();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only creatures are legal death-trigger targets")
    void nonCreatureIsNotALegalTarget() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());

        destroyDiseaseCarriers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(artifact.getId());
    }

    @Test
    @DisplayName("The death trigger is skipped when no creature can be targeted")
    void deathTriggerSkippedWithNoCreatureTargets() {
        harness.addToBattlefield(player1, new DiseaseCarriers());

        destroyDiseaseCarriers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Death trigger does not affect a target that leaves before resolution")
    void deathTriggerFizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        Permanent cinderSeer = addCreatureReady(player2, new CinderSeer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinBerserker());

        destroyDiseaseCarriers();
        harness.handlePermanentChosen(player1, target.getId());

        ScentOfCinder firstRevealedCard = new ScentOfCinder();
        ScentOfCinder secondRevealedCard = new ScentOfCinder();
        harness.setHand(player2, List.of(firstRevealedCard, secondRevealedCard));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        int cinderSeerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(cinderSeer);
        harness.activateAbility(player2, cinderSeerIndex, null, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2,
                List.of(firstRevealedCard.getId(), secondRevealedCard.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Goblin Berserker");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private void destroyDiseaseCarriers() {
        setupPlayer2Active();
        UUID diseaseCarriersId = harness.getPermanentId(player1, "Disease Carriers");
        castScentOfCinder(player2, diseaseCarriersId);
    }

    private void castScentOfCinder(Player player, UUID targetId) {
        ScentOfCinder spell = new ScentOfCinder();
        ScentOfCinder firstRevealedCard = new ScentOfCinder();
        ScentOfCinder secondRevealedCard = new ScentOfCinder();
        ScentOfCinder thirdRevealedCard = new ScentOfCinder();
        harness.setHand(player, List.of(spell, firstRevealedCard, secondRevealedCard, thirdRevealedCard));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castSorcery(player, 0, 0, targetId);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player, List.of(firstRevealedCard.getId(), secondRevealedCard.getId(),
                thirdRevealedCard.getId()));
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
