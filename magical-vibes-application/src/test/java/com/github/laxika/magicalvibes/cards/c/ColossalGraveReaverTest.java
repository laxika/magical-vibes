package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StitchersSupplier;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColossalGraveReaver.class, Forest.class, GrizzlyBears.class, StitchersSupplier.class, Terror.class})
class ColossalGraveReaverTest extends BaseCardTest {

    @Test
    void entersAndReturnsOneCreatureMilledThisWay() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card otherCreature = new GrizzlyBears();
        Card oldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of(creature, land, otherCreature));

        harness.enterBattlefieldAndReturn(player1, new ColossalGraveReaver());
        resolveAllTriggers();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactly(creature, otherCreature);

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(otherCreature));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldCreature, creature, land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == otherCreature);
    }

    @Test
    void attackingMillsThreeCardsAndReturnsACreatureFromThatMillEvent() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card secondCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, land, secondCreature));
        Permanent reaver = addCreatureReady(player1, new ColossalGraveReaver());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(reaver)));
        resolveAllTriggers();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactly(creature, secondCreature);
        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(creature));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, secondCreature);
    }

    @Test
    void landOnlyMillDoesNotReturnAnOlderCreature() {
        Card oldCreature = new GrizzlyBears();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card thirdLand = new Forest();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of(firstLand, secondLand, thirdLand));

        harness.enterBattlefieldAndReturn(player1, new ColossalGraveReaver());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(oldCreature, firstLand, secondLand, thirdLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void emptyLibraryDoesNotReturnAnOlderCreature() {
        Card oldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new ColossalGraveReaver());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void returnsCreatureMilledByAnotherPermanentWithAShortLibrary() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ColossalGraveReaver());
        harness.setLibrary(player1, List.of(creature));

        harness.enterBattlefieldAndReturn(player1, new StitchersSupplier());
        resolveAllTriggers();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactly(creature);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsMillDoesNotTriggerReturn() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ColossalGraveReaver());
        harness.setLibrary(player2, List.of(creature));

        harness.enterBattlefieldAndReturn(player2, new StitchersSupplier());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void pendingReturnCannotFindCreatureThatWasReturnedAndDiedAgain() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ColossalGraveReaver());
        harness.addToBattlefield(player1, new ColossalGraveReaver());
        harness.setLibrary(player1, List.of(creature));

        harness.enterBattlefieldAndReturn(player1, new StitchersSupplier());
        resolveAllTriggers();
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactly(creature);
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returnedCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == creature)
                .findFirst().orElseThrow();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, returnedCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);

        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }
}
