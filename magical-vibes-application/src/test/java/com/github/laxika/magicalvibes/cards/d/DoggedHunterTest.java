package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mirari;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoggedHunter.class, Mirari.class, WoodlandDruid.class})
class DoggedHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dogged Hunter destroys target creature token")
    void destroysCreatureToken() {
        Permanent hunter = addHunter(player1);
        Permanent token = addCreature(player2, true);
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
        Permanent creature = addCreature(player2, false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature token")
    void cannotTargetNoncreatureToken() {
        addHunter(player1);
        Permanent token = addNoncreatureToken(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, token.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapping Dogged Hunter can destroy a creature token it controls")
    void destroysOwnCreatureToken() {
        addHunter(player1);
        Permanent token = addCreature(player1, true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(token.getId()));
    }

    private Permanent addHunter(Player player) {
        return addCreatureReady(player, new DoggedHunter());
    }

    private Permanent addCreature(Player player, boolean token) {
        Card card = new WoodlandDruid();
        card.setToken(token);
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private Permanent addNoncreatureToken(Player player) {
        Card card = new Mirari();
        card.setToken(true);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
