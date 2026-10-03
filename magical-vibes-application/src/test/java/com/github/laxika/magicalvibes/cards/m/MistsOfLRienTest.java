package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistsOfLRien.class, GrizzlyBears.class, HillGiant.class, Island.class, SerraAngel.class})
class MistsOfLRienTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target and each other nonland permanent with the same mana value")
    void returnsTargetAndSameManaValueNonlandPermanents() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent sameManaValue = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent differentManaValue = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent sameManaValueLand = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new MistsOfLRien()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(sameManaValueLand.getId())
                .doesNotContain(target.getId(), differentManaValue.getId());
    }

    @Test
    @DisplayName("Replicate copy may choose a new nonland permanent target")
    void replicateCopyMayChooseNewTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new MistsOfLRien()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{U}"), List.of(originalTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new MistsOfLRien()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }
}
