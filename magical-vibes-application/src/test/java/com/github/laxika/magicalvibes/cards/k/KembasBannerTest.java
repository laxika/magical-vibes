package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KembasBanner.class, GrizzlyBears.class})
class KembasBannerTest extends BaseCardTest {

    @Test
    @DisplayName("For Mirrodin! creates and attaches a Rebel token")
    void forMirrodinCreatesAndAttachesRebel() {
        harness.setHand(player1, List.of(new KembasBanner()));
        addManaForKembasBanner();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent banner = findPermanent(player1, "Kemba's Banner");

        assertThat(rebel.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(3);
        assertThat(banner.getAttachedTo()).isEqualTo(rebel.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each creature its controller controls")
    void equippedCreatureGetsBonusForEachCreatureControlled() {
        Permanent banner = addBannerReady(player1);
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        banner.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, equippedCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The bonus updates when creatures enter and leave the battlefield")
    void bonusUpdatesWithCreatureCount() {
        Permanent banner = addBannerReady(player1);
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        banner.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(3);

        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(otherCreature);
        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip moves Kemba's Banner and its bonus to another creature")
    void equipMovesBannerToAnotherCreature() {
        Permanent banner = addBannerReady(player1);
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        banner.setAttachedTo(firstCreature.getId());
        harness.forceActivePlayer(player1);

        addManaForKembasBanner();
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(banner.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
    }

    private Permanent addBannerReady(Player player) {
        Permanent permanent = new Permanent(new KembasBanner());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player, GrizzlyBears creature) {
        Permanent permanent = new Permanent(creature);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addManaForKembasBanner() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
