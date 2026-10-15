package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VillagersOfEstwald.class})
class VillagersOfEstwaldTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Howlpack of Estwald when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(villagers.isTransformed()).isTrue();
        assertThat(villagers.getCard().getName()).isEqualTo("Howlpack of Estwald");
        assertThat(gqs.getEffectivePower(gd, villagers)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, villagers)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(villagers.isTransformed()).isFalse();
        assertThat(villagers.getCard().getName()).isEqualTo("Villagers of Estwald");
    }

    @Test
    @DisplayName("Howlpack of Estwald transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(villagers.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(villagers.isTransformed()).isFalse();
        assertThat(villagers.getCard().getName()).isEqualTo("Villagers of Estwald");
        assertThat(gqs.getEffectivePower(gd, villagers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, villagers)).isEqualTo(3);
    }

    @Test
    @DisplayName("Howlpack of Estwald does not transform back when only one spell was cast last turn")
    void doesNotTransformBackWithOnlyOneSpellCastLastTurn() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(villagers.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(villagers.isTransformed()).isTrue();
        assertThat(villagers.getCard().getName()).isEqualTo("Howlpack of Estwald");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(villagers.isTransformed()).isTrue();
        assertThat(villagers.getCard().getName()).isEqualTo("Howlpack of Estwald");
    }

    @Test
    @DisplayName("A spell cast by the opponent prevents the front-face upkeep trigger")
    void opponentSpellPreventsTransformTrigger() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(villagers.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The back face stays transformed after a turn with no spells")
    void backFaceDoesNotTriggerAfterSpelllessTurn() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(villagers.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Three spells by its controller transform the back face on that controller's upkeep")
    void transformsBackWhenControllerCastThreeSpells() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player2);
        assertThat(villagers.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 3);
        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(villagers.isTransformed()).isFalse();
        assertThat(villagers.getCard().getName()).isEqualTo("Villagers of Estwald");
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();
    }
}
