package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.InescapableBlaze;
import com.github.laxika.magicalvibes.cards.s.SkylineScout;
import com.github.laxika.magicalvibes.cards.w.WildCeratok;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoxodonRestorer.class, WildCeratok.class, SkylineScout.class, InescapableBlaze.class})
class LoxodonRestorerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 4 life")
    void etbGainsFourLife() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new LoxodonRestorer()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Convoke taps creatures to help pay the generic cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new WildCeratok());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new WildCeratok());
        harness.setHand(player1, List.of(new LoxodonRestorer()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof LoxodonRestorer)
                .hasSize(1);
    }

    @Test
    @DisplayName("Summoning-sick white creatures can convoke the white mana requirements")
    void whiteCreaturesPayColoredCost() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new SkylineScout());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SkylineScout());
        firstCreature.setSummoningSick(true);
        secondCreature.setSummoningSick(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LoxodonRestorer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));
        resolveAllTriggers();

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Loxodon Restorer");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Green creatures cannot convoke the white mana requirements")
    void greenCreaturesCannotPayWhiteCost() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new WildCeratok());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new WildCeratok());
        harness.setHand(player1, List.of(new LoxodonRestorer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Loxodon Restorer");
        harness.assertInHand(player1, "Loxodon Restorer");
    }

    @Test
    @DisplayName("ETB life gain resolves after Restorer dies")
    void lifeGainSurvivesSourceRemoval() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LoxodonRestorer()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        Permanent restorer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LoxodonRestorer)
                .findFirst().orElseThrow();
        harness.setHand(player2, List.of(new InescapableBlaze()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castInstant(player2, 0, restorer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Loxodon Restorer");
        harness.assertInGraveyard(player1, "Loxodon Restorer");
        harness.assertLife(player1, 20);
        resolveAllTriggers();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
