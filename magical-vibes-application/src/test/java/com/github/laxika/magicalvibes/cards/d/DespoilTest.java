package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Despoil.class, RhysticCave.class, DivingGriffin.class})
class DespoilTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and its controller loses 2 life")
    void destroysLandAndControllerLosesLife() {
        harness.addToBattlefield(player2, new RhysticCave());
        harness.setHand(player1, List.of(new Despoil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Rhystic Cave");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Rhystic Cave");
        harness.assertInGraveyard(player2, "Rhystic Cave");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new DivingGriffin());
        harness.setHand(player1, List.of(new Despoil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Diving Griffin");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Can destroy your own land and makes you lose life")
    void destroysOwnLandAndCasterLosesLife() {
        harness.addToBattlefield(player1, new RhysticCave());
        harness.setHand(player1, List.of(new Despoil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Rhystic Cave"));

        harness.assertNotOnBattlefield(player1, "Rhystic Cave");
        harness.assertInGraveyard(player1, "Rhystic Cave");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not cause life loss when the target leaves before resolution")
    void noLifeLossWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new RhysticCave());
        harness.setHand(player1, List.of(new Despoil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Rhystic Cave"));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Despoil");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
