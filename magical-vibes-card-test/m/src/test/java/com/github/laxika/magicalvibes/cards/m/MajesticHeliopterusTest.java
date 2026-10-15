package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.s.SunSentinel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MajesticHeliopterus.class, RaptorCompanion.class, SunSentinel.class})
class MajesticHeliopterusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to another Dinosaur you control")
    void grantsFlyingToAnotherDinosaurYouControl() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new MajesticHeliopterus());
        Permanent target = addCreatureReady(player1, new RaptorCompanion());

        declareAttackers(player1, List.of(0, 1));

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new MajesticHeliopterus());
        Permanent target = addCreatureReady(player1, new RaptorCompanion());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can target a Dinosaur that is not attacking")
    void canTargetNonAttackingDinosaur() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new MajesticHeliopterus());
        Permanent target = addCreatureReady(player1, new RaptorCompanion());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent source = addCreatureReady(player1, new MajesticHeliopterus());
        addCreatureReady(player1, new RaptorCompanion());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, source.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Dinosaur creature")
    void cannotTargetNonDinosaurCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new MajesticHeliopterus());
        addCreatureReady(player1, new RaptorCompanion());
        Permanent nonDinosaur = addCreatureReady(player1, new SunSentinel());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonDinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's Dinosaur")
    void cannotTargetOpponentsDinosaur() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new MajesticHeliopterus());
        addCreatureReady(player1, new RaptorCompanion());
        Permanent opponentDinosaur = addCreatureReady(player2, new RaptorCompanion());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentDinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking alone with no other Dinosaur does not request an illegal target")
    void noOtherDinosaurLeavesNoTargetChoice() {
        addCreatureReady(player1, new MajesticHeliopterus());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Majestic Heliopterus is a legal target")
    void canTargetAnotherCopy() {
        addCreatureReady(player1, new MajesticHeliopterus());
        Permanent target = addCreatureReady(player1, new MajesticHeliopterus());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Removing the source does not stop its attack trigger")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new MajesticHeliopterus());
        Permanent target = addCreatureReady(player1, new RaptorCompanion());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING);
    }
}
