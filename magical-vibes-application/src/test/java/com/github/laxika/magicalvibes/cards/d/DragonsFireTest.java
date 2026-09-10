package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonsFire.class, ChandraNalaar.class, DarksteelColossus.class, ShivanDragon.class})
class DragonsFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage without beholding a Dragon")
    void dealsBaseDamageWithoutBehold() {
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals damage equal to a Dragon card's power when revealed from hand")
    void usesRevealedDragonCardPower() {
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire(), new ShivanDragon()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(), List.of(1));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof ShivanDragon);
    }

    @Test
    @DisplayName("Uses the chosen Dragon's power when it is on the battlefield")
    void usesChosenDragonPower() {
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(dragon.getId()), List.of());
        dragon.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void targetsPlaneswalker() {
        Permanent planeswalker = new Permanent(new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new DragonsFire()));
        addMana();

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
