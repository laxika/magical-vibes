package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmoralBargain.class, GrizzlyBears.class, Forest.class})
class ImmoralBargainTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castWithSacrifices(List<UUID> targetIds, List<UUID> sacrificeIds) {
        gs.playCard(gd, player1, 0, 0, null, null, targetIds, List.of(), false, null,
                null, null, null, null, false, null, null, null, sacrificeIds);
    }

    @Test
    @DisplayName("Sacrificing two creatures destroys two target nonland permanents")
    void sacrificesAndDestroysXNonlandPermanents() {
        Permanent sacrificeOne = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sacrificeTwo = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent targetOne = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetTwo = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        castWithSacrifices(List.of(targetOne.getId(), targetTwo.getId()),
                List.of(sacrificeOne.getId(), sacrificeTwo.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing no creatures destroys no permanents")
    void zeroDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        castWithSacrifices(List.of(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature to set X")
    void cannotSacrificeNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(), List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(land.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }
}
