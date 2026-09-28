package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommanderSofiaDaguerre.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class CommanderSofiaDaguerreTest extends BaseCardTest {

    @Test
    void destroysLegendaryPermanentAndCreatesJunkForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());

        cast(List.of(target.getId()));

        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
        assertThat(countPermanents(player1, "Junk")).isZero();
        assertThat(countPermanents(player2, "Junk")).isEqualTo(1);
        assertThat(findPermanent(player2, "Junk").getCard().getSubtypes())
                .containsExactly(CardSubtype.JUNK);
    }

    @Test
    void canChooseNoTarget() {
        cast(List.of());

        harness.assertOnBattlefield(player1, "Commander Sofia Daguerre");
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    void cannotTargetNonlegendaryPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary");
    }

    private void cast(List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new CommanderSofiaDaguerre()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
