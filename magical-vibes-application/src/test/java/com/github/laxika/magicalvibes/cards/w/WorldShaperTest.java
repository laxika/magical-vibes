package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorldShaperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with World Shaper may mill three cards")
    void attackingMayMillThreeCards() {
        setDeck(player1, List.of(new Forest(), new Forest(), new Forest()));
        addReadyWorldShaper();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining World Shaper's attack trigger does not mill")
    void decliningAttackTriggerDoesNotMill() {
        setDeck(player1, List.of(new Forest(), new Forest(), new Forest()));
        addReadyWorldShaper();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("When World Shaper dies, all land cards in its controller's graveyard return tapped")
    void deathReturnsAllLandsTapped() {
        Card firstForest = new Forest();
        Card secondForest = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstForest, nonland, secondForest));
        Permanent worldShaper = harness.addToBattlefieldAndReturn(player1, new WorldShaper());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, worldShaper));
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "World Shaper");
    }

    private Permanent addReadyWorldShaper() {
        Permanent worldShaper = harness.addToBattlefieldAndReturn(player1, new WorldShaper());
        worldShaper.setSummoningSick(false);
        return worldShaper;
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
