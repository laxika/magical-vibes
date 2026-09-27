package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedressFate.class, DarksteelRelic.class, GloriousAnthem.class,
        GrizzlyBears.class, Mountain.class})
class RedressFateTest extends BaseCardTest {

    @Test
    void returnsArtifactsAndEnchantmentsFromYourGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card artifact = new DarksteelRelic();
        Card enchantment = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        castRedressFate();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(artifact.getId()) || card.getId().equals(enchantment.getId()));
    }

    @Test
    void leavesOtherCardTypesAndOpponentCardsInGraveyards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new GrizzlyBears();
        Card land = new Mountain();
        Card opponentArtifact = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        castRedressFate();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Darksteel Relic");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentArtifact);
    }

    private void castRedressFate() {
        harness.setHand(player1, List.of(new RedressFate()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
