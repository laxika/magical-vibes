package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoltageSurge;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamiyosSafekeeping.class, GrizzlyBears.class, BearerOfMemory.class, Forest.class, VoltageSurge.class})
class TamiyosSafekeepingTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent gains hexproof and indestructible, and you gain 2 life")
    void protectsPermanentAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TamiyosSafekeeping()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Granted keywords expire at the end of the turn")
    void grantedKeywordsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TamiyosSafekeeping()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent")
    void cannotTargetOpponentsPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TamiyosSafekeeping()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Can protect a noncreature permanent")
    void protectsLandAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new TamiyosSafekeeping()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life is gained when the only target leaves before resolution")
    void doesNotGainLifeWhenTargetDiesInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        harness.setHand(player1, List.of(new TamiyosSafekeeping()));
        harness.setHand(player2, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Bearer of Memory");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Tamiyo's Safekeeping");
    }

    @Test
    @DisplayName("Hexproof invalidates an opponent's spell already on the stack")
    void protectsFromOpponentsPendingDamageSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        harness.setHand(player1, List.of(new TamiyosSafekeeping()));
        harness.setHand(player2, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bearer of Memory");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player2, "Voltage Surge");
    }

    @Test
    @DisplayName("Indestructible prevents death from lethal damage by your own spell")
    void survivesLethalDamageFromControllersSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        harness.setHand(player1, List.of(new TamiyosSafekeeping(), new VoltageSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Bearer of Memory");
        harness.assertNotInGraveyard(player1, "Bearer of Memory");
        harness.assertLife(player1, 22);
    }
}
