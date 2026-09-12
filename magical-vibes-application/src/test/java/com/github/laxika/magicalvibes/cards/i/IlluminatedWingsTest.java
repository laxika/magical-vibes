package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.cards.t.ThranFoundry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IlluminatedWings.class, MetathranSoldier.class, ThranFoundry.class})
class IlluminatedWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Illuminated Wings attaches it and grants flying")
    void resolvingAttachesAndGrantsFlying() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new MetathranSoldier());

        harness.setHand(player1, List.of(new IlluminatedWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, soldier.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Illuminated Wings")
                        && p.isAttached()
                        && soldier.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Illuminated Wings grants flying to an opponent's enchanted creature")
    void grantsFlyingToOpponentsCreature() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player2, new MetathranSoldier());

        harness.setHand(player1, List.of(new IlluminatedWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, soldier.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Illuminated Wings draws a card")
    void sacrificingDrawsACard() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new MetathranSoldier());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new IlluminatedWings());
        wings.setAttachedTo(soldier.getId());

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Metathran Soldier");
        harness.assertNotOnBattlefield(player1, "Illuminated Wings");
        harness.assertInGraveyard(player1, "Illuminated Wings");
    }

    @Test
    @DisplayName("Illuminated Wings cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new ThranFoundry());
        harness.setHand(player1, List.of(new IlluminatedWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Thran Foundry");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
