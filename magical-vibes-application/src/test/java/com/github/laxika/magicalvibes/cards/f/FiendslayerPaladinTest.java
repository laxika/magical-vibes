package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiendslayerPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's black spell cannot target Fiendslayer Paladin")
    void opponentBlackSpellCannotTarget() {
        harness.addToBattlefield(player2, new FiendslayerPaladin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fiendslayer Paladin")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of black spells");
    }

    @Test
    @DisplayName("An opponent's red spell cannot target Fiendslayer Paladin")
    void opponentRedSpellCannotTarget() {
        harness.addToBattlefield(player2, new FiendslayerPaladin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fiendslayer Paladin")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of red spells");
    }

    @Test
    @DisplayName("The controller's own red spell can target Fiendslayer Paladin")
    void ownRedSpellCanTarget() {
        harness.addToBattlefield(player1, new FiendslayerPaladin());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Fiendslayer Paladin"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("An opponent's white spell can still target Fiendslayer Paladin")
    void opponentWhiteSpellCanTarget() {
        harness.addToBattlefield(player2, new FiendslayerPaladin());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, harness.getPermanentId(player2, "Fiendslayer Paladin"), null);

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Pacifism"));
    }

    @Test
    @DisplayName("An opponent's black activated ability can still target Fiendslayer Paladin")
    void opponentBlackAbilityCanTarget() {
        Permanent paladin = new Permanent(new FiendslayerPaladin());
        paladin.setSummoningSick(false);
        paladin.tap();
        harness.getGameData().playerBattlefields.get(player1.getId()).add(paladin);

        Permanent assassin = new Permanent(new RoyalAssassin());
        assassin.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(assassin);

        harness.activateAbility(player2, 0, null, paladin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fiendslayer Paladin");
        harness.assertInGraveyard(player1, "Fiendslayer Paladin");
    }
}
