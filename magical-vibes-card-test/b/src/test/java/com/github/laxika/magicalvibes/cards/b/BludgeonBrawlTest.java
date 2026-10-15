package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.t.TitanForge;
import com.github.laxika.magicalvibes.cards.m.MycosynthWellspring;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.t.TamiyosCompleation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService.StaticBonus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BludgeonBrawl.class, BrassSquire.class, DarksteelRelic.class, GrizzlyBears.class, IronMyr.class,
        LeoninScimitar.class, MycosynthWellspring.class, PorcelainLegionnaire.class,
        TamiyosCompleation.class, TitanForge.class})
class BludgeonBrawlTest extends BaseCardTest {

    @Test
    void nonEquipmentArtifactGainsEquipmentSubtype() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new TitanForge());

        StaticBonus bonus = gqs.computeStaticBonus(gd, forge);

        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.EQUIPMENT);
    }

    @Test
    void nonEquipmentArtifactGainsEquipAbility() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new TitanForge());

        StaticBonus bonus = gqs.computeStaticBonus(gd, forge);

        // TitanForge has mana value 3, so equip cost should be {3}
        assertThat(bonus.grantedActivatedAbilities()).hasSize(1);
        assertThat(bonus.grantedActivatedAbilities().getFirst().getManaCost()).isEqualTo("{3}");
        assertThat(bonus.grantedActivatedAbilities().getFirst().getDescription()).isEqualTo("Equip {3}");
    }

    @Test
    void equippedCreatureGetsPowerBoost() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new TitanForge());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        forge.setAttachedTo(bears.getId());

        // GrizzlyBears is 2/2, TitanForge mana value is 3, so creature gets +3/+0
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void multipleArtifactsStackBoost() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent forge1 = harness.addToBattlefieldAndReturn(player1, new TitanForge()); // mana value 3
        Permanent forge2 = harness.addToBattlefieldAndReturn(player1, new TitanForge()); // mana value 3
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        forge1.setAttachedTo(bears.getId());
        forge2.setAttachedTo(bears.getId());

        // 2 base + 3 + 3 = 8 power
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void artifactCreatureNotAffected() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new IronMyr());

        StaticBonus bonus = gqs.computeStaticBonus(gd, myr);

        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.EQUIPMENT);
    }

    @Test
    void naturalEquipmentNotAffected() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        StaticBonus bonus = gqs.computeStaticBonus(gd, scimitar);

        // LeoninScimitar already has Equipment subtype; Bludgeon Brawl should not grant extra abilities
        assertThat(bonus.grantedActivatedAbilities()).isEmpty();
    }

    @Test
    void removingBludgeonBrawlRemovesEffects() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new TitanForge());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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
        Permanent forge = harness.addToBattlefieldAndReturn(player2, new TitanForge());

        StaticBonus bonus = gqs.computeStaticBonus(gd, forge);

        // Bludgeon Brawl affects ALL noncreature non-Equipment artifacts, not just controller's
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.EQUIPMENT);
        assertThat(bonus.grantedActivatedAbilities()).hasSize(1);
    }

    @Test
    @CardUsed({BludgeonBrawl.class, MycosynthWellspring.class, PorcelainLegionnaire.class})
    void grantedEquipResolvesAndChargesManaValue() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent wellspring = harness.addToBattlefieldAndReturn(player1, new MycosynthWellspring());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 1, null, creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(wellspring.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(wellspring.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @CardUsed({BludgeonBrawl.class, MycosynthWellspring.class, PorcelainLegionnaire.class})
    void grantedEquipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player1, new MycosynthWellspring());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PorcelainLegionnaire());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({BludgeonBrawl.class, MycosynthWellspring.class, PorcelainLegionnaire.class})
    void grantedEquipCannotBeActivatedOutsideMainPhase() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        harness.addToBattlefield(player1, new MycosynthWellspring());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({BludgeonBrawl.class, DarksteelRelic.class, PorcelainLegionnaire.class})
    void zeroManaValueArtifactCanEquipWithoutManaAndAddsNoPower() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(relic.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @CardUsed({BludgeonBrawl.class, MycosynthWellspring.class, PorcelainLegionnaire.class,
            TamiyosCompleation.class, BrassSquire.class})
    void equipmentLosingGrantedAbilityNoLongerBoostsCreature() {
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent wellspring = harness.addToBattlefieldAndReturn(player1, new MycosynthWellspring());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        addCreatureReady(player1, new BrassSquire());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, wellspring.getId());
        resolveAllTriggers();

        harness.activateAbilityWithMultiTargets(player1, 3, 0,
                List.of(wellspring.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(wellspring.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.computeStaticBonus(gd, wellspring).losesAllAbilities()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }
}
