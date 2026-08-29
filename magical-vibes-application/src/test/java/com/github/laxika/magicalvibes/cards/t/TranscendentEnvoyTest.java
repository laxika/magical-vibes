package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BondsOfFaith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TranscendentEnvoy.class, BondsOfFaith.class, GrizzlyBears.class})
class TranscendentEnvoyTest extends BaseCardTest {

    @Test
    @DisplayName("Aura spells you cast cost {1} less to cast")
    void auraSpellsCostOneLess() {
        harness.addToBattlefield(player1, new TranscendentEnvoy());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bonds of Faith");
    }

    @Test
    @DisplayName("Non-Aura spells are not reduced")
    void nonAuraSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new TranscendentEnvoy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The cost reduction does not apply to an opponent's Aura spells")
    void opponentAuraSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new TranscendentEnvoy());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.setHand(player2, List.of(new BondsOfFaith()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
