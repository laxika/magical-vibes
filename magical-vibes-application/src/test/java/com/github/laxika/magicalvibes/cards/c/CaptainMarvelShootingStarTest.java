package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainMarvelShootingStar.class, GrizzlyBears.class})
class CaptainMarvelShootingStarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one creature and both life-gain abilities use its power")
    void etbExilesCreatureAndBothAbilitiesUseItsPower() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setPowerModifier(3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CaptainMarvelShootingStar()));
        addCaptainMana();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 25);
    }

    @Test
    @DisplayName("Attack trigger exiles a chosen creature and gains life")
    void attackTriggerExilesCreatureAndGainsLife() {
        addCreatureReady(player1, new CaptainMarvelShootingStar());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 16);
    }

    private void addCaptainMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
