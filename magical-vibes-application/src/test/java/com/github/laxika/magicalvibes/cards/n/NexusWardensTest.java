package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NexusWardens.class, AuraOfSilence.class, GrizzlyBears.class})
class NexusWardensTest extends BaseCardTest {

    @Test
    @DisplayName("You gain 2 life when an enchantment you control enters")
    void gainsLifeWhenYourEnchantmentEnters() {
        addCreatureReady(player1, new NexusWardens());
        harness.setHand(player1, List.of(new AuraOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An opponent's enchantment does not trigger it")
    void doesNotTriggerForOpponentsEnchantment() {
        addCreatureReady(player1, new NexusWardens());
        harness.setHand(player2, List.of(new AuraOfSilence()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature entering does not trigger it")
    void doesNotTriggerForCreature() {
        addCreatureReady(player1, new NexusWardens());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
