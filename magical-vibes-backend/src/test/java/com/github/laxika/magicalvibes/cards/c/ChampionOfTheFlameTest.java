package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerAttachmentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChampionOfTheFlameTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has static +2/+2 per Aura and Equipment effect")
    void hasCorrectEffect() {
        ChampionOfTheFlame card = new ChampionOfTheFlame();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostSelfPerAttachmentEffect.class);
        BoostSelfPerAttachmentEffect effect =
                (BoostSelfPerAttachmentEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
        assertThat(effect.countAuras()).isTrue();
        assertThat(effect.countEquipment()).isTrue();
    }

    // ===== Base stats without attachments =====

    @Test
    @DisplayName("Without attachments, is 1/1")
    void withoutAttachmentsIs1x1() {
        harness.setHand(player1, List.of(new ChampionOfTheFlame()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent champion = findChampion(player1);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
    }

    // ===== With one Equipment =====

    @Test
    @DisplayName("With one Equipment attached, gets +2/+2 from Champion ability plus Equipment stats")
    void withOneEquipment() {
        Permanent champion = addChampionReady(player1);
        Permanent scimitar = addEquipmentReady(player1);
        scimitar.setAttachedTo(champion.getId());

        // Base 1/1 + 2/2 from Champion ability + 1/1 from Leonin Scimitar = 4/4
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(4);
    }

    // ===== With one Aura =====

    @Test
    @DisplayName("With one Aura attached, gets +2/+2 from Champion ability plus Aura stats")
    void withOneAura() {
        Permanent champion = addChampionReady(player1);
        Permanent aura = addAuraReady(player1);
        aura.setAttachedTo(champion.getId());

        // Base 1/1 + 2/2 from Champion ability + 1/2 from Holy Strength = 4/5
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(5);
    }

    // ===== With both Aura and Equipment =====

    @Test
    @DisplayName("With one Aura and one Equipment attached, gets +4/+4 from Champion ability")
    void withAuraAndEquipment() {
        Permanent champion = addChampionReady(player1);
        Permanent scimitar = addEquipmentReady(player1);
        Permanent aura = addAuraReady(player1);
        scimitar.setAttachedTo(champion.getId());
        aura.setAttachedTo(champion.getId());

        // Base 1/1 + 4/4 from Champion (2 attachments) + 1/1 from Scimitar + 1/2 from Holy Strength = 7/8
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(8);
    }

    // ===== Attachments on other creatures don't count =====

    @Test
    @DisplayName("Equipment on other creatures doesn't count for Champion's bonus")
    void attachmentsOnOtherCreaturesDoNotCount() {
        Permanent champion = addChampionReady(player1);
        Permanent otherCreature = addChampionReady(player1);
        Permanent scimitar = addEquipmentReady(player1);

        scimitar.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
    }

    // ===== Unattached Equipment doesn't count =====

    @Test
    @DisplayName("Unattached Equipment on battlefield doesn't affect Champion")
    void unattachedEquipmentDoesNotCount() {
        Permanent champion = addChampionReady(player1);
        addEquipmentReady(player1);

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
    }

    // ===== Multiple Equipment =====

    @Test
    @DisplayName("With two Equipment attached, gets +4/+4 from Champion ability")
    void withTwoEquipment() {
        Permanent champion = addChampionReady(player1);
        Permanent scimitar1 = addEquipmentReady(player1);
        Permanent scimitar2 = addEquipmentReady(player1);

        scimitar1.setAttachedTo(champion.getId());
        scimitar2.setAttachedTo(champion.getId());

        // Base 1/1 + 4/4 from Champion (2 equips) + 1/1 + 1/1 from Scimitars = 7/7
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(7);
    }

    // ===== Helpers =====

    private Permanent findChampion(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Champion of the Flame"))
                .findFirst().orElseThrow();
    }

    private Permanent addChampionReady(Player player) {
        Permanent perm = new Permanent(new ChampionOfTheFlame());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAuraReady(Player player) {
        Permanent perm = new Permanent(new HolyStrength());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
