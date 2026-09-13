package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Submerge.class, Forest.class, GrizzlyBears.class, Island.class})
class SubmergeTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free when an opponent controls a Forest and you control an Island")
    void castsForFreeWithRequiredLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Submerge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(target.getCard());
    }

    @Test
    @DisplayName("Cannot be cast for free without the required land condition")
    void cannotCastForFreeWithoutRequiredLands() {
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Submerge()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast for free when the caster does not control an Island")
    void cannotCastForFreeWithoutOwnIsland() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Submerge()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally when the free-cast condition is not met")
    void castsNormallyWithoutRequiredLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Submerge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("May be cast normally even when the free-cast condition is met")
    void mayCastNormallyWhenFreeCastConditionIsMet() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Submerge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(target.getCard());
    }

    @Test
    @DisplayName("Puts a target creature on top of its owner's library")
    void putsTargetOnOwnersLibrary() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setHand(player1, List.of(new Submerge()));

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(targetCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(targetCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Submerge()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
