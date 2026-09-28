package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrapMastery.class, DarksteelRelic.class, GrizzlyBears.class})
class ScrapMasteryTest extends BaseCardTest {

    private void castScrapMastery() {
        harness.setHand(player1, List.of(new ScrapMastery()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns graveyard artifacts and sacrifices battlefield artifacts")
    void swapsArtifactsWithBattlefields() {
        Permanent ownBattlefieldArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent opponentBattlefieldArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card ownGraveyardArtifact = new DarksteelRelic();
        Card opponentGraveyardArtifact = new DarksteelRelic();
        Card ownGraveyardCreature = new GrizzlyBears();
        Card opponentGraveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownGraveyardArtifact, ownGraveyardCreature));
        harness.setGraveyard(player2, List.of(opponentGraveyardArtifact, opponentGraveyardCreature));

        castScrapMastery();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertOnBattlefield(player2, "Darksteel Relic");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownBattlefieldArtifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentBattlefieldArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ownBattlefieldArtifact.getCard().getId()))
                .contains(ownGraveyardCreature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentBattlefieldArtifact.getCard().getId()))
                .contains(opponentGraveyardCreature);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Leaves nonartifact graveyard cards and permanents alone")
    void leavesNonartifactsAlone() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card ownGraveyardCreature = new GrizzlyBears();
        Card opponentGraveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownGraveyardCreature));
        harness.setGraveyard(player2, List.of(opponentGraveyardCreature));

        castScrapMastery();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownGraveyardCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentGraveyardCreature);
    }
}
