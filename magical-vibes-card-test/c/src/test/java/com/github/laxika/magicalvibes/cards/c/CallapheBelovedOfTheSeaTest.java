package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArmillarySphere;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HeliodsIntervention;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallapheBelovedOfTheSea.class, ArmillarySphere.class, FugitiveWizard.class,
        GloriousAnthem.class, LightningBolt.class, Naturalize.class, ZuranSpellcaster.class,
        HeliodsIntervention.class, Ichthyomorphosis.class, OmenOfTheSea.class})
class CallapheBelovedOfTheSeaTest extends BaseCardTest {

    @Test
    @DisplayName("Callaphe's power equals blue devotion and its toughness is 3")
    void powerEqualsBlueDevotion() {
        Permanent callaphe = addCallaphe();

        assertThat(gqs.getEffectivePower(gd, callaphe)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, callaphe)).isEqualTo(3);

        harness.addToBattlefield(player1, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, callaphe)).isEqualTo(3);
    }

    @Test
    @DisplayName("Callaphe taxes an opponent's spell targeting a creature you control")
    void taxesSpellTargetingCreature() {
        Permanent callaphe = addCallaphe();
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, callaphe.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    @Test
    @DisplayName("Callaphe taxes an opponent's spell targeting an enchantment you control")
    void taxesSpellTargetingEnchantment() {
        addCallaphe();
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        prepareOpponentCast(new Naturalize(), ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, anthem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    @Test
    @DisplayName("Callaphe does not tax an opponent's spell targeting an artifact")
    void doesNotTaxSpellTargetingArtifact() {
        addCallaphe();
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new ArmillarySphere());
        prepareOpponentCast(new Naturalize(), ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, sphere.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Callaphe does not tax an opponent's activated ability")
    void doesNotTaxActivatedAbility() {
        Permanent callaphe = addCallaphe();
        addCreatureReady(player2, new ZuranSpellcaster());
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, callaphe.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each distinct protected target adds its own targeting tax")
    void taxesEachProtectedTarget() {
        Permanent callaphe = addCallaphe();
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheSea());
        prepareOpponentCast(new HeliodsIntervention(), ManaColor.WHITE, 5);

        assertThatThrownBy(() -> gs.playModalXCard(gd, player2, 0, 0, 2, null,
                List.of(callaphe.getId(), omen.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A creature that is also an enchantment receives only one tax ability")
    void enchantmentCreatureIsTaxedOnlyOnce() {
        Permanent callaphe = addCallaphe();
        prepareOpponentCast(new HeliodsIntervention(), ManaColor.WHITE, 4);

        gs.playModalXCard(gd, player2, 0, 0, 1, null, List.of(callaphe.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Callaphe, Beloved of the Sea");
    }

    @Test
    @DisplayName("Your own spells targeting Callaphe are not taxed")
    void doesNotTaxControllersSpell() {
        Permanent callaphe = addCallaphe();
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, callaphe.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, callaphe)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, callaphe)).isEqualTo(1);
    }

    @Test
    @DisplayName("Callaphe stops granting targeting taxes when it loses all abilities")
    void losingAbilitiesRemovesTargetingTax() {
        Permanent callaphe = addCallaphe();
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, callaphe.getId());
        harness.passBothPriorities();
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, callaphe.getId());

        harness.assertInGraveyard(player1, "Callaphe, Beloved of the Sea");
    }

    @Test
    @DisplayName("Devotion counts your enchantments and ignores opponents' permanents")
    void devotionTracksControlledPermanents() {
        Permanent callaphe = addCallaphe();
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheSea());
        harness.addToBattlefield(player2, new OmenOfTheSea());

        assertThat(gqs.getEffectivePower(gd, callaphe)).isEqualTo(3);
        gd.playerBattlefields.get(player1.getId()).remove(omen);
        assertThat(gqs.getEffectivePower(gd, callaphe)).isEqualTo(2);
    }

    @Test
    @DisplayName("Callaphe's power ability works in the graveyard without counting itself")
    void powerAbilityWorksInGraveyard() {
        CallapheBelovedOfTheSea callaphe = new CallapheBelovedOfTheSea();
        harness.setGraveyard(player1, List.of(callaphe));
        harness.addToBattlefield(player1, new OmenOfTheSea());
        harness.addToBattlefield(player2, new OmenOfTheSea());

        assertThat(gqs.getEffectiveCardPower(gd, callaphe)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, callaphe)).isEqualTo(3);
    }

    private Permanent addCallaphe() {
        return harness.addToBattlefieldAndReturn(player1, new CallapheBelovedOfTheSea());
    }

    private void prepareOpponentCast(com.github.laxika.magicalvibes.model.Card spell,
                                     ManaColor color, int amount) {
        harness.ensurePriority(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, color, amount);
    }
}
