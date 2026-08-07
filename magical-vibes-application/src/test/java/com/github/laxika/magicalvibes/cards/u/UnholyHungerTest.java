package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnholyHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target creature without spell mastery and gains no life")
    void destroysWithoutSpellMastery() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        int life = gd.playerLifeTotals.get(player1.getId());

        cast(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life);
    }

    @Test
    @DisplayName("Spell mastery gains 2 life in addition to destroying the creature")
    void spellMasteryGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        int life = gd.playerLifeTotals.get(player1.getId());

        cast(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life + 2);
    }

    @Test
    @DisplayName("Can destroy a creature you control")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void cast(Player player, UUID targetId) {
        harness.setHand(player, List.of(new UnholyHunger()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }
}
