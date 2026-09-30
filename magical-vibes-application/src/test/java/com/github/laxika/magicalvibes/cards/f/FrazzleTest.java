package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GruulScrapper;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.cards.t.TorchDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Frazzle.class, GruulScrapper.class, TorchDrake.class, IzzetSignet.class})
class FrazzleTest extends BaseCardTest {

    @Test
    void countersTargetNonblueSpell() {
        GruulScrapper scrapper = new GruulScrapper();
        harness.castFromHand(player1, scrapper, "{3}{G}");

        harness.setHand(player2, List.of(new Frazzle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, scrapper.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gruul Scrapper");
        harness.assertNotOnBattlefield(player1, "Gruul Scrapper");
    }

    @Test
    void cannotTargetBlueSpell() {
        TorchDrake drake = new TorchDrake();
        harness.castFromHand(player1, drake, "{3}{U}");

        harness.setHand(player2, List.of(new Frazzle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, drake.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblue spell");
    }

    @Test
    void countersTargetColorlessSpell() {
        IzzetSignet signet = new IzzetSignet();
        harness.castFromHand(player1, signet, "{2}");

        harness.setHand(player2, List.of(new Frazzle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, signet.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Izzet Signet");
        harness.assertNotOnBattlefield(player1, "Izzet Signet");
    }
}
