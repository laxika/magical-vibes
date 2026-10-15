package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinnerOfSouls.class, BearCub.class, BurstLightning.class, DayOfJudgment.class, Forest.class, Island.class})
class SpinnerOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("A dying nontoken creature triggers Spinner and finds a creature card")
    void dyingNontokenCreatureFindsCreatureCard() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.addToBattlefield(player1, new BearCub());
        UUID bearsId = harness.getPermanentId(player1, "Bear Cub");
        Forest forest = new Forest();
        Island island = new Island();
        BearCub creature = new BearCub();
        harness.setLibrary(player1, List.of(forest, island, creature));
        killCreatureWithBurstLightning(bearsId);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Bear Cub");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island");
    }

    @Test
    @DisplayName("Declining Spinner of Souls leaves the library unchanged")
    void decliningLeavesLibraryUnchanged() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.addToBattlefield(player1, new BearCub());
        UUID bearsId = harness.getPermanentId(player1, "Bear Cub");
        Forest forest = new Forest();
        BearCub creature = new BearCub();
        harness.setLibrary(player1, List.of(forest, creature));
        killCreatureWithBurstLightning(bearsId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Bear Cub");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, creature);
    }

    @Test
    @DisplayName("A token creature dying does not trigger Spinner of Souls")
    void tokenCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        Permanent token = addTokenCreature(player1);
        harness.setLibrary(player1, List.of(new Forest(), new BearCub()));
        killCreatureWithBurstLightning(token.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unrevealedCardsStayAboveCardsReturnedToBottom() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.addToBattlefield(player1, new BearCub());
        Forest revealed = new Forest();
        BearCub found = new BearCub();
        Island untouched = new Island();
        BearCub laterCreature = new BearCub();
        harness.setLibrary(player1, List.of(revealed, found, untouched, laterCreature));

        killCreatureWithBurstLightning(harness.getPermanentId(player1, "Bear Cub"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(found);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, laterCreature, revealed);
    }

    @Test
    void libraryWithoutCreaturesIsReturnedInItsEntirety() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.addToBattlefield(player1, new BearCub());
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));

        killCreatureWithBurstLightning(harness.getPermanentId(player1, "Bear Cub"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void acceptingWithEmptyLibraryCompletesWithoutAddingCards() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.addToBattlefield(player1, new BearCub());
        harness.setLibrary(player1, List.of());

        killCreatureWithBurstLightning(harness.getPermanentId(player1, "Bear Cub"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.addToBattlefield(player2, new BearCub());
        killCreatureWithBurstLightning(harness.getPermanentId(player2, "Bear Cub"));

        harness.assertInGraveyard(player2, "Bear Cub");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void dyingAloneDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        harness.assertInGraveyard(player1, "Spinner of Souls");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void simultaneousDeathsTriggerForEachOtherNontokenAlly() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.addToBattlefield(player1, new BearCub());
        harness.addToBattlefield(player1, new BearCub());
        addTokenCreature(player1);
        harness.addToBattlefield(player2, new BearCub());
        BearCub first = new BearCub();
        BearCub second = new BearCub();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void killCreatureWithBurstLightning(UUID creatureId) {
        harness.setHand(player2, List.of(new BurstLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, creatureId);
    }

    private Permanent addTokenCreature(Player owner) {
        BearCub tokenCard = new BearCub();
        tokenCard.setToken(true);
        Permanent token = new Permanent(tokenCard);
        gd.playerBattlefields.get(owner.getId()).add(token);
        return token;
    }
}
