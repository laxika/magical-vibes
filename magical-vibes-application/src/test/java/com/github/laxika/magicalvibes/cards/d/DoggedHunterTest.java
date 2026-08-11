package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoggedHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dogged Hunter destroys target creature token")
    void destroysCreatureToken() {
        Permanent hunter = addHunter(player1);
        Permanent token = addCreatureToken(player2, true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, token.getId());

        assertThat(hunter.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(token.getId()));
    }

    @Test
    @DisplayName("Cannot target a nontoken creature")
    void cannotTargetNontokenCreature() {
        addHunter(player1);
        Permanent creature = addCreatureToken(player2, false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature token")
    void cannotTargetNoncreatureToken() {
        addHunter(player1);
        Card clue = new Card();
        clue.setName("Clue");
        clue.setType(CardType.ARTIFACT);
        clue.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, clue);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, token.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHunter(Player player) {
        Permanent hunter = harness.addToBattlefieldAndReturn(player, new DoggedHunter());
        hunter.setSummoningSick(false);
        return hunter;
    }

    private Permanent addCreatureToken(Player player, boolean token) {
        Card card = new Card();
        card.setName("Soldier");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(token);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
