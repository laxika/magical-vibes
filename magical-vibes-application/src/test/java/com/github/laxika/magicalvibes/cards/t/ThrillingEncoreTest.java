package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrillingEncore.class, GrizzlyBears.class, Bonesplitter.class, Annul.class})
class ThrillingEncoreTest extends BaseCardTest {

    private void castThrillingEncore() {
        harness.castFromHand(player1, new ThrillingEncore(), "{4}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns qualifying creature cards from all graveyards under the spell controller's control")
    void returnsCreaturesFromAllGraveyardsUnderYourControl() {
        Card oldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature));

        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, player1Creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, player2Creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, player2Artifact);
        });

        castThrillingEncore();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Bonesplitter");
    }

    @Test
    @DisplayName("Ignores creature cards not put into graveyards from the battlefield this turn")
    void ignoresOldAndNoncreatureCards() {
        Card oldCreature = new GrizzlyBears();
        Card oldInstant = new Annul();
        harness.setGraveyard(player1, List.of(oldCreature, oldInstant));

        castThrillingEncore();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Annul");
    }
}
