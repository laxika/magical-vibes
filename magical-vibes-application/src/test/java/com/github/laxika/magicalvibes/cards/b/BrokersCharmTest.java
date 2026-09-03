package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrokersCharm.class, ChandraNalaar.class, Forest.class, GloriousAnthem.class,
        GrizzlyBears.class, LlanowarElves.class})
class BrokersCharmTest extends BaseCardTest {

    private void addGWU() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Test
    @DisplayName("Mode 0 boosts the creature and uses its boosted power as damage")
    void modeZeroBoostsAndDealsPowerDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new BrokersCharm()));
        addGWU();

        harness.castModalInstant(player1, 0, 0, List.of(
                bear.getId(), harness.getPermanentId(player2, "Llanowar Elves")));
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Mode 0 can damage an opponent's planeswalker")
    void modeZeroDamagesPlaneswalker() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new BrokersCharm()));
        addGWU();

        harness.castModalInstant(player1, 0, 0, List.of(bear.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Nested
    @DisplayName("Mode 1: Destroy target enchantment")
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            harness.addToBattlefield(player2, new GloriousAnthem());
            harness.setHand(player1, List.of(new BrokersCharm()));
            addGWU();

            harness.castInstant(player1, 0, 1,
                    harness.getPermanentId(player2, "Glorious Anthem"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        }

        @Test
        @DisplayName("Cannot target a creature")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new BrokersCharm()));
            addGWU();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                    harness.getPermanentId(player2, "Grizzly Bears")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("enchantment");
        }
    }

    @Test
    @DisplayName("Mode 2 draws two cards")
    void modeTwoDrawsTwoCards() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new BrokersCharm()));
        addGWU();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
