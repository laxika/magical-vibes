package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShowstoppingSurprise.class, GrizzlyBears.class, HillGiant.class})
class ShowstoppingSurpriseTest extends BaseCardTest {

    @Test
    void damagesEachOtherCreatureButNotTheTargetOrPlayers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve(target);

        assertThat(target.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void turnsFaceDownTargetFaceUpBeforeUsingItsPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve(target);

        assertThat(target.isFaceDown()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetCreatureAnOpponentControls() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShowstoppingSurprise()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void faceUpPowerIsUsedAgainstCreatureWithThreeToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addToBattlefield(player2, new HillGiant());

        castAndResolve(target);

        assertThat(target.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void manifestedInstantStaysFaceDownAndStillDealsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShowstoppingSurprise());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castAndResolve(target);

        assertThat(target.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(survivor.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void usesPowerAtResolutionRatherThanWhenCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ShowstoppingSurprise()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, target.getId());
        target.setPowerModifier(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void zeroPowerDealsNoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setPowerModifier(-2);
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(other.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new ShowstoppingSurprise()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
