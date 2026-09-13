package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SealOfDoom;
import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViciousHunger.class, SpinelessThug.class, SealOfDoom.class})
class ViciousHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target creature, killing a 2/2, and controller gains 2 life")
    void dealsDamageAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinelessThug());
        harness.setHand(player1, List.of(new ViciousHunger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Spineless Thug");
        harness.assertInGraveyard(player2, "Spineless Thug");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A tougher creature survives with 2 marked damage; controller still gains 2 life")
    void tougherTargetSurvivesButLifeStillGained() {
        SpinelessThug creature = new SpinelessThug();
        creature.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setHand(player1, List.of(new ViciousHunger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Spineless Thug");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SealOfDoom());
        harness.setHand(player1, List.of(new ViciousHunger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles without gaining life if the target creature leaves before resolution")
    void fizzlesWithoutGainingLifeWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinelessThug());
        harness.setHand(player1, List.of(new ViciousHunger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player1, 20);
    }
}
