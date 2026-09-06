package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CadaverousBloom;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VigilantMartyr.class, CadaverousBloom.class, DarkBanishing.class, Disenchant.class,
        EkunduGriffin.class})
class VigilantMartyrTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing grants a regeneration shield to target creature")
    void sacrificeRegeneratesTargetCreature() {
        addCreatureReady(player1, new VigilantMartyr());
        Permanent griffin = addCreatureReady(player2, new EkunduGriffin());

        harness.activateAbility(player1, 0, null, griffin.getId());
        harness.passBothPriorities();

        assertThat(griffin.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Vigilant Martyr");
    }

    @Test
    @DisplayName("Cannot regenerate a noncreature permanent")
    void cannotRegenerateNoncreaturePermanent() {
        addCreatureReady(player1, new VigilantMartyr());
        harness.addToBattlefield(player1, new CadaverousBloom());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player1, "Cadaverous Bloom")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a spell that targets an enchantment")
    void countersSpellTargetingEnchantment() {
        addCreatureReady(player1, new VigilantMartyr());
        harness.addToBattlefield(player2, new CadaverousBloom());
        harness.addMana(player1, ManaColor.WHITE, 2);

        Disenchant disenchant = new Disenchant();
        harness.setHand(player2, List.of(disenchant));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Cadaverous Bloom"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, disenchant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Disenchant");
        harness.assertOnBattlefield(player2, "Cadaverous Bloom");
        harness.assertInGraveyard(player1, "Vigilant Martyr");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot counter a spell that does not target an enchantment")
    void cannotCounterSpellTargetingCreature() {
        addCreatureReady(player1, new VigilantMartyr());
        harness.addMana(player1, ManaColor.WHITE, 2);

        DarkBanishing darkBanishing = new DarkBanishing();
        harness.setHand(player2, List.of(darkBanishing));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Vigilant Martyr"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, darkBanishing.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the counter ability without {W}{W}")
    void cannotCounterWithoutMana() {
        addCreatureReady(player1, new VigilantMartyr());
        harness.addToBattlefield(player2, new CadaverousBloom());

        Disenchant disenchant = new Disenchant();
        harness.setHand(player2, List.of(disenchant));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Cadaverous Bloom"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, disenchant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
