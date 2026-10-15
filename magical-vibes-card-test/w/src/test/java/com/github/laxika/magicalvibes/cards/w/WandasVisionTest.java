package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WandasVision.class, Forest.class, GrizzlyBears.class, LightningBolt.class,
        BonecrusherGiant.class, Stomp.class})
class WandasVisionTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell exiles until a nonland and offers it for free")
    void secondSpellExilesUntilNonlandAndOffersFreeCast() {
        WandasVision vision = new WandasVision();
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        setUpVision(vision, land, creature);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, creature);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the free cast leaves the exiled cards in exile")
    void decliningFreeCastLeavesCardsExiled() {
        WandasVision vision = new WandasVision();
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        setUpVision(vision, land, creature);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The third spell does not trigger another library dig")
    void thirdSpellDoesNotTriggerAgain() {
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        GrizzlyBears nextCard = new GrizzlyBears();
        setUpVision(new WandasVision(), land, creature);
        harness.setLibrary(player1, List.of(land, creature, nextCard));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A library containing only lands is entirely exiled without a cast offer")
    void allLandLibraryIsExiled() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.addToBattlefield(player1, new WandasVision());
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not offer a free cast")
    void emptyLibraryDoesNotOfferCast() {
        harness.addToBattlefield(player1, new WandasVision());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Spells cast before Wanda's Vision enters count toward the second spell")
    void countsSpellsCastBeforeEntering() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new WandasVision());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An opponent's second spell does not trigger Wanda's Vision")
    void opponentsSecondSpellDoesNotTrigger() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new WandasVision());
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller's second spell triggers on an opponent's turn too")
    void secondSpellOnOpponentsTurnTriggers() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new WandasVision());
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The free cast offers either the creature or its Adventure spell")
    void freeCastOffersAdventureChoice() {
        BonecrusherGiant giant = new BonecrusherGiant();
        harness.addToBattlefield(player1, new WandasVision());
        harness.setLibrary(player1, List.of(giant));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Cast Bonecrusher Giant", "Cast Stomp");
    }

    private void setUpVision(WandasVision vision, Forest land, GrizzlyBears creature) {
        harness.addToBattlefield(player1, vision);
        harness.setLibrary(player1, List.of(land, creature));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
