package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshioksAdept.class, GiantGrowth.class, GrizzlyBears.class, Forest.class})
class AshioksAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic makes each opponent discard a card when targeted")
    void heroicMakesEachOpponentDiscard() {
        Permanent adept = addCreatureReady(player1, new AshioksAdept());
        harness.setHand(player1, new ArrayList<>(List.of(new GiantGrowth(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, adept.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A spell targeting another creature does not trigger heroic")
    void spellTargetingAnotherCreatureDoesNotTriggerHeroic() {
        addCreatureReady(player1, new AshioksAdept());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, otherCreature.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent casting a spell targeting this creature does not trigger heroic")
    void opponentSpellDoesNotTriggerHeroic() {
        Permanent adept = addCreatureReady(player1, new AshioksAdept());
        harness.setHand(player2, new ArrayList<>(List.of(new GiantGrowth(), new GrizzlyBears())));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, adept.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Heroic resolves harmlessly when the opponent has no cards")
    void opponentWithEmptyHandDiscardsNothing() {
        Permanent adept = addCreatureReady(player1, new AshioksAdept());
        harness.setHand(player1, List.of(new GiantGrowth(), new Forest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, adept.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent chooses exactly one card to discard before the spell resolves")
    void opponentChoosesOneCardBeforeSpellResolves() {
        Permanent adept = addCreatureReady(player1, new AshioksAdept());
        harness.setHand(player1, List.of(new GiantGrowth(), new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, adept.getId());
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Giant Growth");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the targeted Adept triggers when its controller has two Adepts")
    void onlyTargetedAdeptTriggers() {
        Permanent adept = addCreatureReady(player1, new AshioksAdept());
        addCreatureReady(player1, new AshioksAdept());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, adept.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
