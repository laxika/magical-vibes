package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FracturedIdentity.class, GrizzlyBears.class, Forest.class})
class FracturedIdentityTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target and creates a copy for each other player")
    void exilesTargetAndCreatesCopyForOtherPlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFracturedIdentity(target);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Excludes the target's controller from receiving a copy")
    void doesNotCreateCopyForTargetController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFracturedIdentity(target);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears"))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Copies remain on the battlefield after the next end step")
    void copiesRemainAfterNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFracturedIdentity(target);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FracturedIdentity()));
        addFracturedIdentityMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFracturedIdentity(Permanent target) {
        harness.setHand(player1, List.of(new FracturedIdentity()));
        addFracturedIdentityMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addFracturedIdentityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
