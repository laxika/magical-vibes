package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeralEncounter.class, GrizzlyBears.class, Forest.class})
class FeralEncounterTest extends BaseCardTest {

    @Test
    void castsWithoutTargetsAndBitesAtTheNextCombat() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card creatureToExile = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                creatureToExile, new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new FeralEncounter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(creatureToExile);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creatureToExile);
        assertThat(gd.exilePlayPermissions).containsEntry(creatureToExile.getId(), player1.getId());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
    }

    @Test
    void noCreatureAmongTopFiveBottomsCardsWithoutAnOrderingChoice() {
        Card untouched = new Forest();
        List<Card> looked = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, List.of(looked.get(0), looked.get(1), looked.get(2),
                looked.get(3), looked.get(4), untouched));
        harness.setHand(player1, List.of(new FeralEncounter()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(looked);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledCreatureCanBeCastByPayingItsNormalCost() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Forest()));
        harness.setHand(player1, List.of(new FeralEncounter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void decliningToExileStillCreatesTheCombatTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new FeralEncounter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
    }

    @Test
    void emptyLibraryStillCreatesTheCombatTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new FeralEncounter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
    }

    @Test
    void mayChooseNoOpposingCreatureForTheCombatTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new FeralEncounter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(victim);
        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(source.getMarkedDamage()).isZero();
    }
}
