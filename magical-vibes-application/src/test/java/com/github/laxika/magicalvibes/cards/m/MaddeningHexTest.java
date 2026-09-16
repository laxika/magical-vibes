package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaddeningHex.class, Shock.class, GrizzlyBears.class})
class MaddeningHexTest extends BaseCardTest {

    @Test
    @DisplayName("Deals a random one-to-six damage when the enchanted player casts a noncreature spell")
    void triggersForEnchantedPlayersNoncreatureSpell() {
        Permanent hex = attachHexToPlayer2();
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Maddening Hex"));

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isBetween(14, 19);
        assertThat(hex.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Does not trigger when the enchanted player casts a creature spell")
    void doesNotTriggerForCreatureSpell() {
        attachHexToPlayer2();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    private Permanent attachHexToPlayer2() {
        Permanent hex = new Permanent(new MaddeningHex());
        hex.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(hex);
        return hex;
    }
}
