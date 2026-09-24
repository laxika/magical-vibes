package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ThePrismaticPiper.class)
class ThePrismaticPiperTest extends BaseCardTest {

    @Test
    void canBeCastFromCommandZone() {
        ThePrismaticPiper piper = new ThePrismaticPiper();
        piper.setOwnerId(player1.getId());
        piper.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), piper);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(piper)));
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
        gd.activePlayerId = player1.getId();
        gd.priorityPassedBy.clear();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.castCommander(gd, player1, piper.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(piper.getId()));
        assertThat(gd.commanderTaxByCardId.get(piper.getId())).isEqualTo(2);
    }
}
