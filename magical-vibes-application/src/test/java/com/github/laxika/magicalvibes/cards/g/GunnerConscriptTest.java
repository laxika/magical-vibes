package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GunnerConscript.class, HolyStrength.class, LeoninScimitar.class, Murder.class})
class GunnerConscriptTest extends BaseCardTest {

    @Test
    void getsPlusOnePlusOneForEachAttachedAuraAndEquipment() {
        Permanent gunner = addGunnerReady();
        Permanent aura = addPermanentReady(player1, new HolyStrength());
        Permanent equipment = addPermanentReady(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gunner)).isEqualTo(2);

        aura.setAttachedTo(gunner.getId());
        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gunner)).isEqualTo(5);

        equipment.setAttachedTo(gunner.getId());
        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gunner)).isEqualTo(7);
    }

    @Test
    void createsOneJunkForEachAttachmentCategoryWhenItDies() {
        Permanent gunner = addGunnerReady();
        Permanent aura = addPermanentReady(player1, new HolyStrength());
        Permanent equipment = addPermanentReady(player1, new LeoninScimitar());
        aura.setAttachedTo(gunner.getId());
        equipment.setAttachedTo(gunner.getId());

        destroy(gunner);

        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }

    @Test
    void createsNoJunkWhenItDiesWithoutAttachments() {
        Permanent gunner = addGunnerReady();

        destroy(gunner);

        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    private Permanent addGunnerReady() {
        return addPermanentReady(player1, new GunnerConscript());
    }

    private Permanent addPermanentReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void destroy(Permanent gunner) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, gunner.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
