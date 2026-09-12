package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagisterOfWorth.class, GrizzlyBears.class})
class MagisterOfWorthTest extends BaseCardTest {

    @Test
    void graceMajorityReturnsEachPlayersCreatureCards() {
        Card player1Creature = new GrizzlyBears();
        Card player2Creature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(player1Creature)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(player2Creature)));
        castMagister(new MagisterOfWorth());

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.GraceOrCondemnationChoice.GRACE);
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.GraceOrCondemnationChoice.GRACE);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == player2Creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void condemnationWinsOnTieAndDestroysAllOtherCreatures() {
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card magister = new MagisterOfWorth();
        castMagister(magister);

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == magister)
                .findFirst().orElseThrow();

        harness.handleListChoice(player1, ChoiceContext.GraceOrCondemnationChoice.GRACE);
        harness.handleListChoice(player2, ChoiceContext.GraceOrCondemnationChoice.CONDEMNATION);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2Creature);
    }

    private void castMagister(Card magister) {
        harness.setHand(player1, List.of(magister));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.GraceOrCondemnationChoice.OPTIONS);
        return choice;
    }
}
