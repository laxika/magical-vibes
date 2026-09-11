package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrontlineStrategist.class, GrizzlyBears.class, YotianSoldier.class, ProdigalSorcerer.class})
class FrontlineStrategistTest extends BaseCardTest {

    @Test
    @DisplayName("Turning face up prevents combat damage from non-Soldier creatures")
    void turningFaceUpPreventsCombatDamageFromNonSoldiers() {
        turnFrontlineStrategistFaceUp();
        harness.setLife(player2, 20);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, soldier, true)).isFalse();

        declareAttackers(player1, List.of(1, 2));
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Turning face up does not prevent noncombat damage")
    void turningFaceUpDoesNotPreventNoncombatDamage() {
        turnFrontlineStrategistFaceUp();
        harness.setLife(player1, 20);

        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    private void turnFrontlineStrategistFaceUp() {
        harness.setHand(player1, List.of(new FrontlineStrategist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent strategist = findPermanent(player1, "Frontline Strategist");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(strategist));
        harness.passBothPriorities();
    }
}
