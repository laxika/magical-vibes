package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
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

@CardUsed({TyvarsStand.class, GrizzlyBears.class})
class TyvarsStandTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +X/+X, hexproof, and indestructible")
    void givesBoostAndProtection() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bears, 3);

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(bears.getGrantedKeywords())
                .contains(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("X=0 still grants hexproof and indestructible")
    void xZeroStillGrantsKeywords() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bears, 0);

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getGrantedKeywords())
                .contains(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Boost and keywords wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bears, 2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getGrantedKeywords())
                .doesNotContain(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TyvarsStand()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("You can target your hexproof creature again and the boosts add together")
    void repeatedCastsAddBoosts() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(bears, 2);
        castResolve(bears, 3);

        assertThat(bears.getEffectivePower()).isEqualTo(7);
        assertThat(bears.getEffectiveToughness()).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getGrantedKeywords())
                .doesNotContain(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @CardUsed({Terror.class})
    @DisplayName("Hexproof prevents an opponent from targeting the protected creature")
    void hexproofPreventsOpponentTargeting() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(bears, 0);
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({Terror.class})
    @DisplayName("Granting hexproof in response makes opposing removal fail to resolve")
    void protectsAgainstRemovalAlreadyOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, bears.getId());

        castResolve(bears, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Terror");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Terror.class})
    @DisplayName("Your own removal can target the creature but indestructible prevents destruction")
    void indestructiblePreventsDestroyEffect() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(bears, 0);
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Terror");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Incinerate.class})
    @DisplayName("Indestructible prevents lethal damage from destroying the creature")
    void indestructiblePreventsLethalDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(bears, 0);
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Incinerate");
        assertThat(bears.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed({Terror.class})
    @DisplayName("The spell does not grant any effects when its target is removed in response")
    void removedTargetReceivesNoEffects() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TyvarsStand()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, 3, bears.getId());
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Tyvar's Stand");
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getGrantedKeywords())
                .doesNotContain(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE);
        assertThat(gd.stack).isEmpty();
    }

    private void castResolve(Permanent target, int x) {
        harness.setHand(player1, List.of(new TyvarsStand()));
        harness.addMana(player1, ManaColor.GREEN, x + 1);
        harness.castInstant(player1, 0, x, target.getId());
        harness.passBothPriorities();
    }
}
