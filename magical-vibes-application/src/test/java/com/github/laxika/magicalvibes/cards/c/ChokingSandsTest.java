package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChokingSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a basic non-Swamp land without dealing damage")
    void destroysBasicLandNoDamage() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new ChokingSands()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player2, "Plains");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Destroys a nonbasic land and deals 2 damage to its controller")
    void destroysNonbasicLandAndDealsDamage() {
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.setHand(player1, List.of(new ChokingSands()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Field of Ruin");
        harness.assertInGraveyard(player2, "Field of Ruin");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target a Swamp")
    void cannotTargetSwamp() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new ChokingSands()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID swampId = harness.getPermanentId(player2, "Swamp");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, swampId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Swamp land");
    }

    @Test
    @DisplayName("Fizzles if the target land leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.setHand(player1, List.of(new ChokingSands()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
