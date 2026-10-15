package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.a.AzimaetDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        KaerveksPurge.class,
        AdamantWill.class,
        AzimaetDrake.class,
        Forest.class,
        Regeneration.class,
        RestInPeace.class
})
class KaerveksPurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature with mana value X and deals its power to its controller")
    void destroysCreatureAndDealsPowerDamage() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake()).getId();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 4); // {X=3}{B}{R}
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 3, target);

        harness.assertInGraveyard(player2, "Azimaet Drake");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Deals no damage when the creature survives destruction")
    void indestructibleCreatureTakesNoDamage() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake()).getId();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new AdamantWill(), new KaerveksPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target);
        harness.castAndResolveSorcery(player1, 0, 3, target);

        harness.assertOnBattlefield(player2, "Azimaet Drake");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value does not equal X")
    void cannotTargetCreatureWithDifferentManaValue() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake()).getId();

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses the creature's boosted power immediately before it dies")
    void usesLastKnownBoostedPower() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake()).getId();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 3, target);

        harness.assertInGraveyard(player2, "Azimaet Drake");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can destroy the caster's creature and damage the caster")
    void damagesControllerOfOwnCreature() {
        UUID target = harness.addToBattlefieldAndReturn(player1, new AzimaetDrake()).getId();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 3, target);

        harness.assertInGraveyard(player1, "Azimaet Drake");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Regeneration prevents destruction and the damage rider")
    void regeneratedCreatureDealsNoDamage() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake()).getId();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Regeneration(), new KaerveksPurge()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, target);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 3, target);

        harness.assertOnBattlefield(player2, "Azimaet Drake");
        harness.assertNotInGraveyard(player2, "Azimaet Drake");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Exiling the creature instead of putting it into a graveyard prevents damage")
    void exileReplacementPreventsDamage() {
        harness.addToBattlefield(player1, new RestInPeace());
        UUID target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake()).getId();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 3, target);

        harness.assertNotOnBattlefield(player2, "Azimaet Drake");
        harness.assertNotInGraveyard(player2, "Azimaet Drake");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Azimaet Drake");
        harness.assertLife(player2, 20);
    }
}
