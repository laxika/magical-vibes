package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AshioksAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic makes each opponent discard a card when targeted")
    void heroicMakesEachOpponentDiscard() {
        Permanent adept = addCreatureReady(player1, new AshioksAdept());
        harness.setHand(player1, new ArrayList<>(List.of(new GiantGrowth(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, adept.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, otherCreature.getId());
        harness.passBothPriorities();

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

        harness.castInstant(player2, 0, adept.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
