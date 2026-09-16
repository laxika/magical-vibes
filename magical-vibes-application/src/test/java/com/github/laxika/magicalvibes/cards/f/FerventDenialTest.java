package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FerventDenial.class, AvenFisher.class})
class FerventDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when cast normally")
    void countersSpellNormally() {
        AvenFisher fisher = new AvenFisher();

        harness.setHand(player2, List.of(new FerventDenial()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castFromHand(player1, fisher, "{3}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fisher.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Fisher");
        harness.assertInGraveyard(player2, "Fervent Denial");
    }

    @Test
    @DisplayName("Flashback counters a spell and exiles Fervent Denial")
    void flashbackCountersSpellAndExilesIt() {
        AvenFisher fisher = new AvenFisher();
        harness.setGraveyard(player2, List.of(new FerventDenial()));
        harness.addMana(player2, ManaColor.BLUE, 7);

        harness.castFromHand(player1, fisher, "{3}{U}");
        harness.passPriority(player1);
        harness.castFlashback(player2, 0, fisher.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Aven Fisher");
        harness.assertNotInGraveyard(player2, "Fervent Denial");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fervent Denial"));
    }
}
