package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingPoltergeist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CripplingChill.class, RagingPoltergeist.class, Forest.class, Cloudshift.class})
class CripplingChillTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target creature, locks its next untap step and draws a card")
    void tapsAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RagingPoltergeist());
        harness.setHand(player1, List.of(new CripplingChill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLibrary(player1, List.of(new RagingPoltergeist()));

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        harness.assertInHand(player1, "Raging Poltergeist");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CripplingChill()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the target controller's next untap step is skipped")
    void skipsOnlyNextControllerUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingPoltergeist());
        harness.setHand(player1, List.of(new CripplingChill()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped creature you control can be targeted and still draws a card")
    void targetsAlreadyTappedOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RagingPoltergeist());
        target.tap();
        harness.setHand(player1, List.of(new CripplingChill()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two copies before the same untap step do not skip two untap steps")
    void repeatedCastsSkipSameUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingPoltergeist());
        harness.setHand(player1, List.of(new CripplingChill(), new CripplingChill()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature that leaves and returns before resolution is a new object and no card is drawn")
    void blinkedTargetPreventsDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingPoltergeist());
        harness.setHand(player1, List.of(new CripplingChill()));
        harness.setHand(player2, List.of(new Cloudshift()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        gs.passPriority(gd, player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Raging Poltergeist");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Crippling Chill");
    }
}
