package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
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

class SpellbaneCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Blue spells cannot target Spellbane Centaur")
    void blueSpellsCannotTarget() {
        harness.addToBattlefield(player2, new SpellbaneCentaur());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Spellbane Centaur")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of blue");
    }

    @Test
    @DisplayName("Abilities from blue sources cannot target Spellbane Centaur")
    void blueSourceAbilitiesCannotTarget() {
        harness.addToBattlefield(player2, new SpellbaneCentaur());

        Permanent sorcerer = new Permanent(new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(sorcerer);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Spellbane Centaur")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue");
    }

    @Test
    @DisplayName("Non-blue spells can target Spellbane Centaur")
    void nonBlueSpellsCanTarget() {
        harness.addToBattlefield(player2, new SpellbaneCentaur());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Spellbane Centaur"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Shock"));
    }
}
