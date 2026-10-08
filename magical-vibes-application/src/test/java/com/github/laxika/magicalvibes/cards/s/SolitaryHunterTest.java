package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolitaryHunter.class})
class SolitaryHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Solitary Hunter transforms when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent hunter = addCreatureReady(player1, new SolitaryHunter());
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
        Permanent hunter = addCreatureReady(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(hunter.isTransformed()).isFalse();
        assertThat(hunter.getCard().getName()).isEqualTo("Solitary Hunter");
    }

    @Test
    @DisplayName("One of the Pack transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsCastLastTurn() {
        Permanent hunter = addCreatureReady(player1, new SolitaryHunter());
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
        Permanent hunter = addCreatureReady(player1, new SolitaryHunter());
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
        Permanent hunter = addCreatureReady(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.clear();

        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(hunter.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("An opponent's spell prevents the front face from triggering")
    void opponentSpellPreventsTransformTrigger() {
        Permanent hunter = addCreatureReady(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(hunter.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Each Solitary Hunter transforms independently on the same upkeep")
    void multipleHuntersTransformIndependently() {
        Permanent first = addCreatureReady(player1, new SolitaryHunter());
        Permanent second = addCreatureReady(player2, new SolitaryHunter());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTransformed()).isTrue();
        assertThat(second.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The back face does not trigger when no spells were cast last turn")
    void noSpellsDoNotTransformBack() {
        Permanent hunter = addCreatureReady(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(hunter.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(hunter.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Two spells by the controller transform the back face on their upkeep")
    void controllerTwoSpellsTransformBack() {
        Permanent hunter = addCreatureReady(player1, new SolitaryHunter());
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(hunter.isTransformed()).isTrue();
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(hunter.isTransformed()).isFalse();
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }
}
