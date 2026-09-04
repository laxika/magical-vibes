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

@CardUsed({FoggySwampVisions.class, GrizzlyBears.class})
class FoggySwampVisionsTest extends BaseCardTest {

    @Test
    void copiesCreatureCardsFromAllGraveyardsAndSacrificesTokensAtNextEndStep() {
        Permanent waterbendSourceOne = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent waterbendSourceTwo = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        gs.playCard(gd, player1, 0, 2, null, null,
                List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(waterbendSourceOne.getId(), waterbendSourceTwo.getId()));

        assertThat(waterbendSourceOne.isTapped()).isTrue();
        assertThat(waterbendSourceTwo.isTapped()).isTrue();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownCreature.getId(), opposingCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opposingCreature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void xZeroDoesNotCreateTokens() {
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
