package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmergentSequenceTest extends BaseCardTest {

    @Test
    void searchesForABasicLandAnimatesItAndCountsLandsEnteredThisTurn() {
        harness.setHand(player1, List.of(new EmergentSequence()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(),
                new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .findFirst()
                .orElseThrow();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(2);
        assertThat(forest.getEffectiveToughness()).isEqualTo(2);
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(forest.getGrantedSubtypes()).contains(CardSubtype.FRACTAL);
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    void mayFailToFindABasicLand() {
        harness.setHand(player1, List.of(new EmergentSequence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
