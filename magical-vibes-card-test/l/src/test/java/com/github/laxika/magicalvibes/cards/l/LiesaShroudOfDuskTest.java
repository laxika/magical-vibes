package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LiesaShroudOfDusk.class, Fog.class})
class LiesaShroudOfDuskTest extends BaseCardTest {

    @Test
    @DisplayName("The player who casts a spell loses 2 life")
    void spellCasterLosesLife() {
        harness.addToBattlefield(player1, new LiesaShroudOfDusk());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int casterLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player2, new Fog(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(casterLifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("The controller loses 2 life when they cast a spell")
    void controllerLosesLifeWhenCastingSpell() {
        harness.addToBattlefield(player1, new LiesaShroudOfDusk());

        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new Fog(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void commandZoneCastingPaysLifeInsteadOfAdditionalMana(int previousCasts) {
        LiesaShroudOfDusk commander = prepareCommander(previousCasts);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.getLife(player1.getId());

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2 * previousCasts);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Liesa, Shroud of Dusk");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2 * previousCasts);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void availableManaCannotReplaceMandatoryCommanderLifePayment(int previousCasts) {
        LiesaShroudOfDusk commander = prepareCommander(previousCasts);
        harness.addMana(player1, ManaColor.COLORLESS, 2 + 2 * previousCasts);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.getLife(player1.getId());

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2 * previousCasts);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(2 * previousCasts);
    }

    @Test
    void castingLiesaFromHandDoesNotTriggerHerOwnLifeLoss() {
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new LiesaShroudOfDusk(), "{2}{W}{W}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Liesa, Shroud of Dusk");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    private LiesaShroudOfDusk prepareCommander(int previousCasts) {
        LiesaShroudOfDusk commander = new LiesaShroudOfDusk();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        gd.commanderTaxByCardId.put(commander.getId(), 2 * previousCasts);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);
        return commander;
    }
}
