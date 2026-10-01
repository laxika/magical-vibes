package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChardalynDragon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarningTremor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonehideAncientWarningTremor.class, WarningTremor.class, ChardalynDragon.class,
        GrizzlyBears.class, Forest.class})
class StonehideAncientWarningTremorTest extends BaseCardTest {

    @Test
    void warningTremorDealsDamageAndReducesTheNextDragonSpell() {
        StonehideAncientWarningTremor card = new StonehideAncientWarningTremor();
        ChardalynDragon firstDragon = new ChardalynDragon();
        ChardalynDragon secondDragon = new ChardalynDragon();
        harness.setHand(player1, List.of(card, firstDragon, secondDragon));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Chardalyn Dragon")).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void stonehideAncientReturnsAllNonDragonCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new ChardalynDragon());
        StonehideAncientWarningTremor stonehide = new StonehideAncientWarningTremor();
        harness.setHand(player1, List.of(stonehide));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(stonehide);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getCard)
                .containsExactly(dragon.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(ownBear.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentBear.getCard());
    }

    @Test
    void warningTremorCannotTargetAland() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new StonehideAncientWarningTremor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
