package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevouringTendrils.class, GarrukWildspeaker.class, GrizzlyBears.class, HillGiant.class})
class DevouringTendrilsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the source creature's power to an opposing creature and gains life when it dies")
    void damagesCreatureAndGainsLifeWhenItDies() {
        Permanent source = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(source);
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevouringTendrils()));
        addMana();

        harness.castSorcery(player1, 0, List.of(source.getId(), victim.getId()));
        resolveStack();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The delayed trigger gains life when an opposing planeswalker dies")
    void gainsLifeWhenPlaneswalkerDies() {
        Permanent source = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(source);
        Permanent planeswalker = new Permanent(new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new DevouringTendrils()));
        addMana();

        harness.castSorcery(player1, 0, List.of(source.getId(), planeswalker.getId()));
        resolveStack();

        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target a permanent you control as the victim")
    void cannotTargetOwnPermanentAsVictim() {
        Permanent source = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(source);
        Permanent victim = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(victim);
        harness.setHand(player1, List.of(new DevouringTendrils()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }
}
