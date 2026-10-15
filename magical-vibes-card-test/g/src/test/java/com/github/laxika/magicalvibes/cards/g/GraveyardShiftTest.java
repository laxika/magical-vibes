package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveyardShift.class, CivilServant.class, GirderGoons.class, Mountain.class, Murder.class, Strangle.class})
class GraveyardShiftTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from your graveyard to the battlefield")
    void returnsTargetCreatureFromGraveyard() {
        Card creature = new CivilServant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GraveyardShift()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can be cast at instant speed with five distinct mana values in your graveyard")
    void fiveDistinctManaValuesGrantFlashTiming() {
        Card target = new CivilServant();
        harness.setGraveyard(player1, List.of(
                new Mountain(), new Strangle(), target, new Murder(), new GirderGoons()));
        castDuringOpponentsTurn(target);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without five distinct mana values")
    void fewerThanFiveDistinctManaValuesKeepSorceryTiming() {
        Card target = new CivilServant();
        harness.setGraveyard(player1, List.of(new Mountain(), new Strangle(), target, new Murder()));
        prepareToCastDuringOpponentsTurn();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Five cards with only four distinct mana values do not grant flash")
    void duplicateManaValuesDoNotGrantFlash() {
        Card target = new CivilServant();
        harness.setGraveyard(player1, List.of(
                new Mountain(), new Strangle(), target, new Murder(), new CivilServant()));
        prepareToCastDuringOpponentsTurn();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Mana values in the opponent's graveyard do not grant flash")
    void opponentsGraveyardDoesNotGrantFlash() {
        Card target = new CivilServant();
        harness.setGraveyard(player1, List.of(target));
        harness.setGraveyard(player2, List.of(
                new Mountain(), new Strangle(), new CivilServant(), new Murder(), new GirderGoons()));
        prepareToCastDuringOpponentsTurn();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a creature card in the opponent's graveyard")
    void rejectsOpponentsCreature() {
        Card target = new CivilServant();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GraveyardShift()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void rejectsNoncreatureCard() {
        Card target = new Murder();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new GraveyardShift()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not return a target that leaves the graveyard before resolution")
    void targetLeavingGraveyardPreventsReturn() {
        Card target = new CivilServant();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new GraveyardShift()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, target.getId());

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Civil Servant");
        harness.assertInHand(player1, "Civil Servant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the flash condition after casting does not stop resolution")
    void flashConditionIsNotRequiredAtResolution() {
        Card target = new CivilServant();
        harness.setGraveyard(player1, List.of(
                new Mountain(), new Strangle(), target, new Murder(), new GirderGoons()));
        castDuringOpponentsTurn(target);

        harness.setGraveyard(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Civil Servant");
        harness.assertNotInGraveyard(player1, "Civil Servant");
        assertThat(gd.stack).isEmpty();
    }

    private void castDuringOpponentsTurn(Card target) {
        prepareToCastDuringOpponentsTurn();
        harness.castSorcery(player1, 0, target.getId());
    }

    private void prepareToCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GraveyardShift()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passPriority(player2);
    }
}
