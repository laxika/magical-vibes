package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.e.EmbermawHellion;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.s.SoulfireGrandMaster;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Damage the steps of a simultaneous damage event deal is held until the event ends and dealt
 * combined per recipient and source; a replacement-order prompt for that combined damage pauses the
 * resolution and is answered without dealing any damage twice.
 */
@CardUsed({AjanisPridemate.class, Char.class, EmbermawHellion.class, FurnaceOfRath.class,
        SoulfireGrandMaster.class})
class SimultaneousDamageMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("Each recipient orders the replacements for its share of simultaneous damage, dealt once")
    void combinedDamagePromptsEachRecipientAndDealsOnce() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Nothing is dealt until the opponent orders the replacements for its 4 damage.
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        chooseBonusFirst(player2);

        // (4 + 1) * 2 to the opponent is dealt; Char's 2 damage to its caster waits for that player.
        harness.assertLife(player2, 10);
        harness.assertLife(player1, 20);
        chooseBonusFirst(player1);

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 14);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Char");
    }

    @Test
    @DisplayName("Lifelink from simultaneous damage that needed replacement prompts is gained once")
    void lifelinkAfterPromptsIsOneGain() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        Permanent pridemate = harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseBonusFirst(player2);
        chooseBonusFirst(player1);

        // 10 to the opponent and 6 to its caster: 20 - 6 + 16.
        harness.assertLife(player1, 30);
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void chooseBonusFirst(Player chooser) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(chooser.getId());
        harness.handleListChoice(chooser, choice.options().stream()
                .filter(option -> option.startsWith("Deal 1 more damage"))
                .findFirst().orElseThrow());
    }
}
