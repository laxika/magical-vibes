package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireProphecy.class, GrizzlyBears.class, HillGiant.class, Mountain.class, Shock.class})
class FireProphecyTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireProphecy()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void mayPutCardOnBottomThenDraws() {
        Shock bottom = new Shock();
        Shock draw = new Shock();
        Card keep = new HillGiant();
        harness.setHand(player1, List.of(new FireProphecy(), bottom, keep));
        harness.setLibrary(player1, List.of(draw));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(bottom.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep, draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom);
    }

    @Test
    void mayDeclineToPutCardOnBottom() {
        Shock draw = new Shock();
        Card keep = new HillGiant();
        harness.setHand(player1, List.of(new FireProphecy(), keep));
        harness.setLibrary(player1, List.of(draw));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void cannotTargetAPlayerOrLand() {
        harness.setHand(player1, List.of(new FireProphecy()));
        addMana();
        harness.addToBattlefield(player2, new Mountain());

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Mountain")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
