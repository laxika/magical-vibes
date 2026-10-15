package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.OrazcaRaptor;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.s.SlipperyScoundrel;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TilonallisCrown.class, OrazcaRaptor.class, RaptorCompanion.class,
        SlipperyScoundrel.class, StriderHarness.class})
class TilonallisCrownTest extends BaseCardTest {

    @Test
    void etbDamagesAndBoostsEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new OrazcaRaptor());

        harness.setHand(player1, List.of(new TilonallisCrown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void effectsStopWhenAuraIsRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new OrazcaRaptor());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TilonallisCrown());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotEnchantNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new StriderHarness());

        harness.setHand(player1, List.of(new TilonallisCrown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void entryDamageKillsOpponentsOneToughnessCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new TilonallisCrown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Raptor Companion");
        harness.assertInGraveyard(player2, "Raptor Companion");
        harness.assertNotOnBattlefield(player1, "Tilonalli's Crown");
        harness.assertInGraveyard(player1, "Tilonalli's Crown");
    }

    @Test
    void entryDamageStillAppliesWhenEnchantedCreatureGainsHexproof() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SlipperyScoundrel());
        harness.setHand(player1, List.of(new TilonallisCrown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        gd.playersWithCityBlessing.add(player2.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        resolveAllTriggers();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Tilonalli's Crown");
    }

    @Test
    void entryDamageUsesCurrentEnchantedCreatureWhenAuraMoves() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new OrazcaRaptor());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new OrazcaRaptor());
        harness.setHand(player1, List.of(new TilonallisCrown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof TilonallisCrown)
                .findFirst().orElseThrow();
        aura.setAttachedTo(destination.getId());
        resolveAllTriggers();

        assertThat(original.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, destination)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, destination, Keyword.TRAMPLE)).isTrue();
    }
}
