package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEquipmentAttachedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinGaveleerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has static +2/+0 per equipment effect")
    void hasCorrectEffect() {
        GoblinGaveleer card = new GoblinGaveleer();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostSelfPerEquipmentAttachedEffect.class);
        BoostSelfPerEquipmentAttachedEffect effect =
                (BoostSelfPerEquipmentAttachedEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerPerEquipment()).isEqualTo(2);
        assertThat(effect.toughnessPerEquipment()).isEqualTo(0);
    }

    // ===== Base stats without equipment =====

    @Test
    @DisplayName("Without equipment, is 1/1")
    void withoutEquipmentIs1x1() {
        harness.setHand(player1, List.of(new GoblinGaveleer()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gaveleer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Gaveleer"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(1);
    }

    // ===== With one equipment =====

    @Test
    @DisplayName("With one equipment attached, is 4/2 (+2/+0 from Gaveleer, +1/+1 from Scimitar)")
    void withOneEquipmentIs4x2() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(gaveleer.getId());

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(2);
    }

    // ===== With multiple equipment =====

    @Test
    @DisplayName("With two equipment attached, is 7/3 (+4/+0 from Gaveleer, +2/+2 from two Scimitars)")
    void withTwoEquipmentIs7x3() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent scimitar1 = addScimitarReady(player1);
        Permanent scimitar2 = addScimitarReady(player1);

        scimitar1.setAttachedTo(gaveleer.getId());
        scimitar2.setAttachedTo(gaveleer.getId());

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(3);
    }

    // ===== Equipment on other creatures doesn't count =====

    @Test
    @DisplayName("Equipment on other creatures doesn't count for Goblin Gaveleer's bonus")
    void equipmentOnOtherCreaturesDoesntCount() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent otherCreature = addGaveleerReady(player1);
        Permanent scimitar = addScimitarReady(player1);

        scimitar.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(1);
    }

    // ===== Equipment not attached doesn't count =====

    @Test
    @DisplayName("Unattached equipment on battlefield doesn't affect Goblin Gaveleer")
    void unattachedEquipmentDoesntCount() {
        Permanent gaveleer = addGaveleerReady(player1);
        addScimitarReady(player1);

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(1);
    }

    // ===== Opponent's equipment DOES count =====

    @Test
    @DisplayName("Opponent's equipment DOES affect Goblin Gaveleer (+2/+0 from Gaveleer, +1/+1 from Scimitar)")
    void opponentEquipmentAffects() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent scimitar = addScimitarReady(player2);
        scimitar.setAttachedTo(gaveleer.getId());

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addGaveleerReady(Player player) {
        Permanent perm = new Permanent(new GoblinGaveleer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addScimitarReady(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
