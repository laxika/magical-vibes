package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarriedDronesmith.class, Murder.class, EsixFractalBloom.class})
class HarriedDronesmithTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty 1/1 colorless Thopter artifact creature token at the beginning of your combat")
    void createsHastyThopterAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new HarriedDronesmith());

        advanceToCombat(player1);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new HarriedDronesmith());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices the token at the beginning of the next end step")
    void sacrificesTokenAtNextEndStep() {
        harness.addToBattlefield(player1, new HarriedDronesmith());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("A combat trigger creates and later sacrifices its token even if Dronesmith is destroyed in response")
    void triggerSurvivesSourceRemoval() {
        Permanent dronesmith = harness.addToBattlefieldAndReturn(player1, new HarriedDronesmith());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        advanceToCombat(player1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, dronesmith.getId());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Harried Dronesmith");
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("Each Dronesmith creates its own token and both tokens are sacrificed")
    void multipleDronesmithsCreateAndSacrificeSeparateTokens() {
        harness.addToBattlefield(player1, new HarriedDronesmith());
        harness.addToBattlefield(player1, new HarriedDronesmith());

        advanceToCombat(player1);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(findPermanents(player1, "Thopter")).hasSize(2);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        assertThat(findPermanents(player1, "Harried Dronesmith")).hasSize(2);
    }

    @Test
    @DisplayName("Tokens created through Esix's replacement still gain haste from Dronesmith")
    void replacementTokenGainsHaste() {
        harness.addToBattlefield(player1, new HarriedDronesmith());
        harness.addToBattlefield(player1, new EsixFractalBloom());
        Permanent creatureToCopy = harness.addToBattlefieldAndReturn(player2, new HarriedDronesmith());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creatureToCopy.getId());

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
