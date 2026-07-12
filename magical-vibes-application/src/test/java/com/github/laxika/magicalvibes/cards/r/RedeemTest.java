package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedeemTest extends BaseCardTest {

    private void castRedeem(List<UUID> targets) {
        harness.setHand(player1, List.of(new Redeem()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targets);
        harness.passBothPriorities();
    }

    private void shock(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Prevents all damage to both target creatures this turn")
    void protectsTwoCreatures() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRedeem(List.of(a.getId(), b.getId()));

        // Shock (2 damage) each protected 2/2 — both survive.
        shock(a);
        shock(b);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("May target only one creature (up to two)")
    void protectsOneCreature() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRedeem(List.of(a.getId()));
        shock(a);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Prevention wears off after turn cleanup")
    void wearsOff() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRedeem(List.of(a.getId()));

        // Simulate end-of-turn cleanup clearing the one-turn prevention shield.
        gd.creaturesWithAllDamagePrevented.clear();

        shock(a);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Redeem()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
