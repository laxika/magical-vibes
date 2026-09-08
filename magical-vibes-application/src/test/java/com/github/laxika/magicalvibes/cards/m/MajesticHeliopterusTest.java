package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MajesticHeliopterusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to another Dinosaur you control")
    void grantsFlyingToAnotherDinosaurYouControl() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new MajesticHeliopterus());
        Permanent target = addReadyCreature(player1, new FrenziedRaptor());

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

        addReadyCreature(player1, new MajesticHeliopterus());
        Permanent target = addReadyCreature(player1, new FrenziedRaptor());

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

        addReadyCreature(player1, new MajesticHeliopterus());
        Permanent target = addReadyCreature(player1, new FrenziedRaptor());

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

        Permanent source = addReadyCreature(player1, new MajesticHeliopterus());
        addReadyCreature(player1, new FrenziedRaptor());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, source.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Dinosaur creature")
    void cannotTargetNonDinosaurCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new MajesticHeliopterus());
        addReadyCreature(player1, new FrenziedRaptor());
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's Dinosaur")
    void cannotTargetOpponentsDinosaur() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new MajesticHeliopterus());
        addReadyCreature(player1, new FrenziedRaptor());
        Permanent opponentDinosaur = addReadyCreature(player2, new FrenziedRaptor());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentDinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
