package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.ManholeMissile;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManholeMissile.class, GrizzlyBears.class, HillGiant.class, Shock.class, Mountain.class})
class ManholeMissileTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ManholeMissile()));

        castAt(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void mayPutCardOnBottomThenDraws() {
        Shock bottom = new Shock();
        Shock draw = new Shock();
        Card keep = new HillGiant();
        harness.setHand(player1, List.of(new ManholeMissile(), bottom, keep));
        harness.setLibrary(player1, List.of(draw));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castAt(target);

        harness.handleMultipleCardsChosen(player1, List.of(bottom.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep, draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom);
    }

    @Test
    void mayDeclineToPutCardOnBottom() {
        Shock draw = new Shock();
        Card keep = new HillGiant();
        harness.setHand(player1, List.of(new ManholeMissile(), keep));
        harness.setLibrary(player1, List.of(draw));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castAt(target);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void cannotTargetAPlayerOrLand() {
        harness.setHand(player1, List.of(new ManholeMissile()));
        addMana();
        harness.addToBattlefield(player2, new Mountain());

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Mountain")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsDuringResolutionWithoutAnotherPriorityPass() {
        Shock bottom = new Shock();
        Mountain draw = new Mountain();
        harness.setHand(player1, List.of(new ManholeMissile(), bottom));
        harness.setLibrary(player1, List.of(draw));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castAt(target);
        harness.handleMultipleCardsChosen(player1, List.of(bottom.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void canDrawTheBottomedCardFromAnEmptyLibrary() {
        Mountain bottom = new Mountain();
        harness.setHand(player1, List.of(new ManholeMissile(), bottom));
        harness.setLibrary(player1, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAt(target);
        harness.handleMultipleCardsChosen(player1, List.of(bottom.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotOfferBottomingWhenItsOnlyTargetBecomesIllegal() {
        Mountain keep = new Mountain();
        Shock draw = new Shock();
        harness.setHand(player1, List.of(new ManholeMissile(), keep));
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player2, List.of(new Shock()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertInGraveyard(player1, "Manhole Missile");
    }

    private void castAt(Permanent target) {
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
