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
        harness.castAndResolveInstant(player2, 0, fisher.getId());

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
        harness.castAndResolveFlashback(player2, 0, fisher.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Aven Fisher");
        harness.assertNotInGraveyard(player2, "Fervent Denial");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fervent Denial"));
    }

    @Test
    @DisplayName("Can counter its controller's own spell")
    void countersOwnSpell() {
        AvenFisher fisher = new AvenFisher();
        harness.castFromHand(player1, fisher, "{3}{U}");
        harness.setHand(player1, List.of(new FerventDenial()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0, fisher.getId());

        harness.assertInGraveyard(player1, "Aven Fisher");
        harness.assertInGraveyard(player1, "Fervent Denial");
        harness.assertNotOnBattlefield(player1, "Aven Fisher");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("A countered flashback spell is exiled and its target still resolves")
    void counteredFlashbackIsExiled() {
        AvenFisher fisher = new AvenFisher();
        FerventDenial flashbackDenial = new FerventDenial();
        harness.setGraveyard(player2, List.of(flashbackDenial));
        harness.addMana(player2, ManaColor.BLUE, 7);
        harness.castFromHand(player1, fisher, "{3}{U}");
        harness.passPriority(player1);
        harness.castFlashback(player2, 0, fisher.getId());
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new FerventDenial()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0, flashbackDenial.getId());

        harness.assertNotInGraveyard(player2, "Fervent Denial");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getId()).contains(flashbackDenial.getId());
        harness.assertInGraveyard(player1, "Fervent Denial");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aven Fisher");
        harness.assertNotInGraveyard(player1, "Aven Fisher");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
