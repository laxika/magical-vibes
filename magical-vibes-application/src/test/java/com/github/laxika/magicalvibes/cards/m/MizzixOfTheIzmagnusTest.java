package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MizzixOfTheIzmagnus.class, Divination.class, GrizzlyBears.class, Shock.class})
class MizzixOfTheIzmagnusTest extends BaseCardTest {

    @Test
    void gainsExperienceOnlyForInstantOrSorcerySpellsWithGreaterManaValue() {
        harness.addToBattlefield(player1, new MizzixOfTheIzmagnus());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
    }

    @Test
    void reducesInstantAndSorceryCostsByCurrentExperienceCounters() {
        harness.addToBattlefield(player1, new MizzixOfTheIzmagnus());
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 3);
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    void doesNotReduceCreatureSpells() {
        harness.addToBattlefield(player1, new MizzixOfTheIzmagnus());
        gd.playerExperienceCounters.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesOnlyTheControllersInstantAndSorcerySpells() {
        harness.addToBattlefield(player1, new MizzixOfTheIzmagnus());
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
