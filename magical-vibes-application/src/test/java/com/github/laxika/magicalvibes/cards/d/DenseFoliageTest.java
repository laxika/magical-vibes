package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DenseFoliageTest extends BaseCardTest {

    @Test
    @DisplayName("Spells cannot target a creature while Dense Foliage is out")
    void spellsCannotTargetCreatures() {
        harness.addToBattlefield(player1, new DenseFoliage());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    @DisplayName("Spells can still target players while Dense Foliage is out")
    void spellsCanStillTargetPlayers() {
        harness.addToBattlefield(player1, new DenseFoliage());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Without Dense Foliage a spell can target the creature")
    void spellsTargetCreaturesWithoutDenseFoliage() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Activated abilities can still target creatures while Dense Foliage is out")
    void abilitiesCanStillTargetCreatures() {
        harness.addToBattlefield(player1, new DenseFoliage());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);

        Permanent assassin = new Permanent(new RoyalAssassin());
        assassin.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(assassin);

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
