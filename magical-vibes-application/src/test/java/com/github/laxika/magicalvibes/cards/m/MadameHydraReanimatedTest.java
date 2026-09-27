package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadameHydraReanimated.class, Forest.class, GrizzlyBears.class})
class MadameHydraReanimatedTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each creature card in its controller's graveyard")
    void boostsForCreatureCardsInOwnGraveyard() {
        Permanent hydra = addCreatureReady(player1, new MadameHydraReanimated());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters by milling two cards")
    void entersAndMillsTwoCards() {
        List<Card> milledCards = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, milledCards);

        harness.enterBattlefieldAndReturn(player1, new MadameHydraReanimated());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milledCards);
    }

    @Test
    @DisplayName("Attacking mills two cards")
    void attackMillsTwoCards() {
        List<Card> milledCards = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, milledCards);
        addCreatureReady(player1, new MadameHydraReanimated());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milledCards);
    }
}
