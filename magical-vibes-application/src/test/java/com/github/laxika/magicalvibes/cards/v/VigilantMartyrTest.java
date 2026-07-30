package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VigilantMartyrTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing grants a regeneration shield to target creature")
    void sacrificeRegeneratesTargetCreature() {
        addCreatureReady(player1, new VigilantMartyr());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Vigilant Martyr");
    }

    @Test
    @DisplayName("Counters a spell that targets an enchantment")
    void countersSpellTargetingEnchantment() {
        addCreatureReady(player1, new VigilantMartyr());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.WHITE, 2);

        Disenchant disenchant = new Disenchant();
        harness.setHand(player2, List.of(disenchant));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Glorious Anthem"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, disenchant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Disenchant");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Vigilant Martyr");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot counter a spell that does not target an enchantment")
    void cannotCounterSpellTargetingCreature() {
        addCreatureReady(player1, new VigilantMartyr());
        harness.addMana(player1, ManaColor.WHITE, 2);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Vigilant Martyr"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the counter ability without {W}{W}")
    void cannotCounterWithoutMana() {
        addCreatureReady(player1, new VigilantMartyr());
        harness.addToBattlefield(player1, new GloriousAnthem());

        Disenchant disenchant = new Disenchant();
        harness.setHand(player2, List.of(disenchant));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Glorious Anthem"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, disenchant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
