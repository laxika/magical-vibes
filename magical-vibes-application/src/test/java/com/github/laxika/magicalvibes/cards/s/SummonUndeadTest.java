package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonUndead.class, Forest.class, GrizzlyBears.class})
class SummonUndeadTest extends BaseCardTest {

    @Test
    void acceptingMayMillsThenReturnsCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castSummonUndead();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void decliningMayStillReturnsCreatureWithoutMilling() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castSummonUndead();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    private void castSummonUndead() {
        harness.setHand(player1, List.of(new SummonUndead()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
