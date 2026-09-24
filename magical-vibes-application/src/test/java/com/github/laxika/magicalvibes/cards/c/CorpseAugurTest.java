package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseAugur.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
class CorpseAugurTest extends BaseCardTest {

    @Test
    @DisplayName("When Corpse Augur dies, controller draws and target player loses life for creature cards in that graveyard")
    void deathTriggerCountsTargetPlayersCreatureCards() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new FountainOfYouth()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new CorpseAugur());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int targetLifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Corpse Augur"));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(targetLifeBefore - 2);
    }
}
