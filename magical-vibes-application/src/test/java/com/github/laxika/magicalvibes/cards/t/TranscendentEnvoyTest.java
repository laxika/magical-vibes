package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.IndomitableWill;
import com.github.laxika.magicalvibes.cards.i.IroassBlessing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TranscendentEnvoy.class, IndomitableWill.class, IroassBlessing.class})
class TranscendentEnvoyTest extends BaseCardTest {

    @Test
    @DisplayName("Aura spells you cast cost {1} less to cast")
    void auraSpellsCostOneLess() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TranscendentEnvoy());
        harness.setHand(player1, List.of(new IndomitableWill()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(IndomitableWill.class);
    }

    @Test
    @DisplayName("Non-Aura spells are not reduced")
    void nonAuraSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new TranscendentEnvoy());
        harness.setHand(player1, List.of(new TranscendentEnvoy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The cost reduction does not apply to an opponent's Aura spells")
    void opponentAuraSpellsAreNotReduced() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TranscendentEnvoy());
        harness.setHand(player2, List.of(new IndomitableWill()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleEnvoysStackTheirReductions() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TranscendentEnvoy());
        harness.addToBattlefield(player1, new TranscendentEnvoy());
        harness.setHand(player1, List.of(new IroassBlessing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessReductionDoesNotPayColoredMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TranscendentEnvoy());
        harness.addToBattlefield(player1, new TranscendentEnvoy());
        harness.setHand(player1, List.of(new IndomitableWill()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void reductionStopsWhenEnvoyLeavesBattlefield() {
        Permanent departed = harness.addToBattlefieldAndReturn(player1, new TranscendentEnvoy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TranscendentEnvoy());
        gd.playerBattlefields.get(player1.getId()).remove(departed);
        gd.playerGraveyards.get(player1.getId()).add(departed.getCard());
        harness.setHand(player1, List.of(new IroassBlessing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
