package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SacredFire.class, CandlegroveWitch.class})
class SacredFireTest extends BaseCardTest {

    @Test
    void dealsDamageToPlayerAndGainsLife() {
        harness.setHand(player1, List.of(new SacredFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 17);
    }

    @Test
    void dealsDamageToCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new CandlegroveWitch());
        harness.setHand(player1, List.of(new SacredFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Candlegrove Witch"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
        harness.assertLife(player1, 17);
    }

    @Test
    void flashbackDealsDamageGainsLifeAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new SacredFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 17);
        harness.assertNotInGraveyard(player1, "Sacred Fire");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sacred Fire"));
    }
    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new SacredFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Sacred Fire");
    }

    @Test
    void gainsNoLifeWhenItsOnlyTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new CandlegroveWitch());
        harness.setHand(player1, List.of(new SacredFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Candlegrove Witch"));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Sacred Fire");
    }

    @Test
    void flashbackIsExiledWithoutLifeGainWhenItsOnlyTargetLeaves() {
        harness.addToBattlefield(player2, new CandlegroveWitch());
        harness.setGraveyard(player1, List.of(new SacredFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 15);

        harness.castFlashback(player1, 0, harness.getPermanentId(player2, "Candlegrove Witch"));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertNotInGraveyard(player1, "Sacred Fire");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sacred Fire"));
    }
}
