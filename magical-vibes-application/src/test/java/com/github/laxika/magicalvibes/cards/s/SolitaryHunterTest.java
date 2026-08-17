package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SolitaryHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Solitary Hunter transforms when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent hunter = addReadyPermanent(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.clear();

        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(hunter.isTransformed()).isTrue();
        assertThat(hunter.getCard().getName()).isEqualTo("One of the Pack");
        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(6);
    }

    @Test
    @DisplayName("Solitary Hunter does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent hunter = addReadyPermanent(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(hunter.isTransformed()).isFalse();
        assertThat(hunter.getCard().getName()).isEqualTo("Solitary Hunter");
    }

    @Test
    @DisplayName("One of the Pack transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsCastLastTurn() {
        Permanent hunter = addReadyPermanent(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(hunter.isTransformed()).isFalse();
        assertThat(hunter.getCard().getName()).isEqualTo("Solitary Hunter");
        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(4);
    }

    @Test
    @DisplayName("One of the Pack does not transform back when no player cast two spells last turn")
    void doesNotTransformBackWhenFewerThanTwoSpellsCastLastTurn() {
        Permanent hunter = addReadyPermanent(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(hunter.isTransformed()).isTrue();
        assertThat(hunter.getCard().getName()).isEqualTo("One of the Pack");
    }

    @Test
    @DisplayName("The transform ability triggers during an opponent's upkeep")
    void transformsDuringOpponentsUpkeep() {
        Permanent hunter = addReadyPermanent(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.clear();

        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(hunter.isTransformed()).isTrue();
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
