package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitaniaProtectorOfArgoth.class, Forest.class, Mountain.class, StoneRain.class})
class TitaniaProtectorOfArgothTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target land card from the graveyard to the battlefield")
    void entersAndReturnsTargetLand() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new TitaniaProtectorOfArgoth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A land you control going to the graveyard creates a 5/3 Elemental")
    void landGoingToGraveyardCreatesElemental() {
        harness.addToBattlefield(player1, new TitaniaProtectorOfArgoth());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, mountain.getId());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(5);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(3);
    }
}
