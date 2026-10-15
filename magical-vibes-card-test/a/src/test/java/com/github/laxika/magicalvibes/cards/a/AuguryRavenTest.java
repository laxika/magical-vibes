package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuguryRaven.class})
class AuguryRavenTest extends BaseCardTest {

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(raven.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, raven.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Augury Raven");
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, raven.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellWithoutPayingTwoMana() {
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Augury Raven");
        assertThat(gd.findExiledCard(raven.getId())).isNull();
    }

    @Test
    void foretellIsAllowedOutsideMainPhaseAndDoesNotUseTheStack() {
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void foretoldCreatureStillRequiresMainPhaseTiming() {
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, raven.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void foretellCastingRequiresBlueMana() {
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, raven.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

}
