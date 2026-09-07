package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuskbursterSwarm.class, GrizzlyBears.class, Shock.class})
class HuskbursterSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each owned creature card in the graveyard and exile")
    void costsLessForOwnedCreatureCardsInGraveyardAndExile() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setExile(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setExile(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HuskbursterSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Huskburster Swarm");
    }

    @Test
    @DisplayName("Does not count noncreature cards or creature cards owned by an opponent")
    void doesNotCountNoncreatureOrOpponentOwnedCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setExile(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setExile(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HuskbursterSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
