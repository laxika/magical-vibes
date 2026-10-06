package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.cards.j.JeskaiSage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamanOfTheGreatHunt.class, FeralKrushok.class, JeskaiSage.class})
class ShamanOfTheGreatHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature that deals combat damage")
    void countersEachCombatDamageDealer() {
        Permanent shaman = addCreatureReady(player1, new ShamanOfTheGreatHunt());
        Permanent sage = addCreatureReady(player1, new JeskaiSage());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ferocious ability draws for each qualifying creature")
    void drawsForEachCreatureWithPowerFourOrGreater() {
        addCreatureReady(player1, new ShamanOfTheGreatHunt());
        harness.addToBattlefield(player1, new FeralKrushok());
        harness.addToBattlefield(player1, new JeskaiSage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JeskaiSage(), new JeskaiSage(), new JeskaiSage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draw ability can be activated without a qualifying creature and draws zero")
    void canActivateWithoutCreatureWithPowerFourOrGreater() {
        Permanent shaman = addCreatureReady(player1, new ShamanOfTheGreatHunt());
        shaman.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JeskaiSage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draw count uses current power at resolution and ignores opposing creatures")
    void countsQualifyingCreaturesAtResolution() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new ShamanOfTheGreatHunt());
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new JeskaiSage());
        harness.addToBattlefield(player2, new FeralKrushok());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JeskaiSage(), new JeskaiSage()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        shaman.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draw ability resolves for zero when no creature still qualifies")
    void drawsZeroWhenPowerDropsBeforeResolution() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new ShamanOfTheGreatHunt());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JeskaiSage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        shaman.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Hybrid activation accepts mixed green and blue mana while tapped")
    void acceptsMixedHybridManaWhileTapped() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new ShamanOfTheGreatHunt());
        shaman.setTapped(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JeskaiSage(), new JeskaiSage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
