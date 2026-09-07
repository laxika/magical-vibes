package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArmillarySphere;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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
        GloriousAnthem.class, LightningBolt.class, Naturalize.class, ZuranSpellcaster.class})
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
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, callaphe.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addCallaphe() {
        return harness.addToBattlefieldAndReturn(player1, new CallapheBelovedOfTheSea());
    }

    private void prepareOpponentCast(com.github.laxika.magicalvibes.model.Card spell,
                                     ManaColor color, int amount) {
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, color, amount);
    }
}
