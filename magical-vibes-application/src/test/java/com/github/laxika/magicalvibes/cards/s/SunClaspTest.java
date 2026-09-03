package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.cards.u.UndiscoveredParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunClasp.class, LongbowArcher.class, UndiscoveredParadise.class})
class SunClaspTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sun Clasp attaches it and gives the creature +1/+3")
    void resolvingAttachesAndBoosts() {
        Permanent archer = addCreatureReady(player1, new LongbowArcher());

        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, archer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof SunClasp
                        && p.isAttached()
                        && p.getAttachedTo().equals(archer.getId()));
        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(5);
    }

    @Test
    @DisplayName("Sun Clasp does not boost other creatures")
    void doesNotBoostOtherCreatures() {
        Permanent archer = addCreatureReady(player1, new LongbowArcher());
        Permanent otherArcher = addCreatureReady(player1, new LongbowArcher());

        Permanent clasp = harness.addToBattlefieldAndReturn(player1, new SunClasp());
        clasp.setAttachedTo(archer.getId());

        assertThat(gqs.getEffectivePower(gd, otherArcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherArcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating {W} returns enchanted creature to hand; Aura dies as orphaned")
    void activatedAbilityBouncesEnchantedCreature() {
        Permanent archer = addCreatureReady(player1, new LongbowArcher());

        Permanent clasp = harness.addToBattlefieldAndReturn(player1, new SunClasp());
        clasp.setAttachedTo(archer.getId());

        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(3);

        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof LongbowArcher);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof LongbowArcher || p.getCard() instanceof SunClasp);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SunClasp);
    }

    @Test
    @DisplayName("Sun Clasp fizzles if the target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent archer = addCreatureReady(player1, new LongbowArcher());

        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, archer.getId());
        gd.playerBattlefields.get(player1.getId()).remove(archer);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SunClasp);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SunClasp);
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new LongbowArcher());
        harness.addToBattlefield(player1, new UndiscoveredParadise());
        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent paradise = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof UndiscoveredParadise)
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, paradise.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Returning an opponent's enchanted creature puts it in its owner's hand")
    void returnsOpponentCreatureToItsOwnersHand() {
        LongbowArcher archerCard = new LongbowArcher();
        archerCard.setOwnerId(player2.getId());
        Permanent archer = addCreatureReady(player2, archerCard);

        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, archer.getId());
        harness.passBothPriorities();

        Permanent clasp = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof SunClasp)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(3);

        harness.addMana(player1, ManaColor.WHITE, 1);
        int claspIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clasp);
        harness.activateAbility(player1, claspIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card instanceof LongbowArcher);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard() instanceof LongbowArcher);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SunClasp);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SunClasp);
    }
}
