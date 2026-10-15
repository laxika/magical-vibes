package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionOfTheFlame.class, HolyStrength.class, LeoninScimitar.class})
class ChampionOfTheFlameTest extends BaseCardTest {

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

    @Test
    @DisplayName("Unattached Equipment on battlefield doesn't affect Champion")
    void unattachedEquipmentDoesNotCount() {
        Permanent champion = addChampionReady(player1);
        addEquipmentReady(player1);

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
    }

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

    @Test
    @DisplayName("Auras controlled by an opponent count toward the bonus")
    void opponentControlledAuraCounts() {
        Permanent champion = addChampionReady(player1);
        Permanent aura = addAuraReady(player2);
        aura.setAttachedTo(champion.getId());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(5);
    }

    @Test
    @DisplayName("Each attached Aura counts separately and removed Auras stop counting")
    void multipleAurasAndRemoval() {
        Permanent champion = addChampionReady(player1);
        Permanent first = addAuraReady(player1);
        Permanent second = addAuraReady(player1);
        first.setAttachedTo(champion.getId());
        second.setAttachedTo(champion.getId());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(9);

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(5);
    }

    @Test
    @DisplayName("Moving Equipment immediately updates both Champions' bonuses")
    void movingEquipmentUpdatesBonuses() {
        Permanent first = addChampionReady(player1);
        Permanent second = addChampionReady(player1);
        Permanent scimitar = addEquipmentReady(player1);
        scimitar.setAttachedTo(first.getId());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);

        scimitar.setAttachedTo(second.getId());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    private Permanent findChampion(Player player) {
        return findPermanent(player, "Champion of the Flame");
    }

    private Permanent addChampionReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChampionOfTheFlame());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addAuraReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HolyStrength());
        perm.setSummoningSick(false);
        return perm;
    }
}
