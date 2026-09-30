package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SalvationColossus.class, GrizzlyBears.class})
class SalvationColossusTest extends BaseCardTest {

    @Test
    void attackTriggerBoostsOtherOwnCreaturesAndGrantsIndestructible() {
        Permanent colossus = addCreatureReady(player1, new SalvationColossus());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void attackBonusWearsOffAtEndOfTurn() {
        Permanent colossus = addCreatureReady(player1, new SalvationColossus());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void energyUnearthReturnsThisCardWithHasteAndExilesItAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new SalvationColossus()));
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Salvation Colossus");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Salvation Colossus");
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card ->
                card.getName().equals("Salvation Colossus"));
    }

    @Test
    void cannotUnearthWithoutEightEnergy() {
        harness.setGraveyard(player1, List.of(new SalvationColossus()));
        gd.playerEnergyCounters.put(player1.getId(), 7);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
