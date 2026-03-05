package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.t.TitanForge;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService.StaticBonus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BludgeonBrawlTest extends BaseCardTest {

    @Test
    void nonEquipmentArtifactGainsEquipmentSubtype() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = gd.playerBattlefields.get(player1.getId()).get(1);
        StaticBonus bonus = gqs.computeStaticBonus(gd, forge);

        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.EQUIPMENT);
    }

    @Test
    void nonEquipmentArtifactGainsEquipAbility() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = gd.playerBattlefields.get(player1.getId()).get(1);
        StaticBonus bonus = gqs.computeStaticBonus(gd, forge);

        // TitanForge has mana value 3, so equip cost should be {3}
        assertThat(bonus.grantedActivatedAbilities()).hasSize(1);
        assertThat(bonus.grantedActivatedAbilities().getFirst().getManaCost()).isEqualTo("{3}");
        assertThat(bonus.grantedActivatedAbilities().getFirst().getDescription()).isEqualTo("Equip {3}");
    }

    @Test
    void equippedCreatureGetsPowerBoost() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player1, new TitanForge());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent forge = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(2);
        forge.setAttachedTo(bears.getId());

        // GrizzlyBears is 2/2, TitanForge mana value is 3, so creature gets +3/+0
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void multipleArtifactsStackBoost() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player1, new TitanForge()); // mana value 3
        harness.addToBattlefield(player1, new TitanForge()); // mana value 3
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent forge1 = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent forge2 = gd.playerBattlefields.get(player1.getId()).get(2);
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(3);
        forge1.setAttachedTo(bears.getId());
        forge2.setAttachedTo(bears.getId());

        // 2 base + 3 + 3 = 8 power
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void artifactCreatureNotAffected() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player1, new IronMyr());

        Permanent myr = gd.playerBattlefields.get(player1.getId()).get(1);
        StaticBonus bonus = gqs.computeStaticBonus(gd, myr);

        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.EQUIPMENT);
    }

    @Test
    void naturalEquipmentNotAffected() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent scimitar = gd.playerBattlefields.get(player1.getId()).get(1);
        StaticBonus bonus = gqs.computeStaticBonus(gd, scimitar);

        // LeoninScimitar already has Equipment subtype; Bludgeon Brawl should not grant extra abilities
        assertThat(bonus.grantedActivatedAbilities()).isEmpty();
    }

    @Test
    void removingBludgeonBrawlRemovesEffects() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player1, new TitanForge());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent forge = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(2);
        forge.setAttachedTo(bears.getId());

        // Verify boost is active
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        // Remove Bludgeon Brawl
        gd.playerBattlefields.get(player1.getId()).removeFirst();

        // Boost should be gone
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        // Equipment subtype should also be gone
        StaticBonus bonus = gqs.computeStaticBonus(gd, forge);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.EQUIPMENT);
    }

    @Test
    void affectsOpponentArtifactsToo() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player2, new TitanForge());

        Permanent forge = gd.playerBattlefields.get(player2.getId()).getFirst();
        StaticBonus bonus = gqs.computeStaticBonus(gd, forge);

        // Bludgeon Brawl affects ALL noncreature non-Equipment artifacts, not just controller's
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.EQUIPMENT);
        assertThat(bonus.grantedActivatedAbilities()).hasSize(1);
    }
}
