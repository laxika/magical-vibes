package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TsaboTavocTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Tsabo Tavoc destroys a target legendary creature")
    void destroysTargetLegendaryCreature() {
        addTsaboTavoc();
        Permanent arvad = addTarget(new ArvadTheCursed());
        arvad.setRegenerationShield(1);
        addBlackMana();

        harness.activateAbility(player1, 0, null, arvad.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Arvad the Cursed");
        harness.assertInGraveyard(player2, "Arvad the Cursed");
    }

    @Test
    @DisplayName("Cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        addTsaboTavoc();
        Permanent bears = addTarget(new GrizzlyBears());
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    private Permanent addTarget(Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player2.getId()).add(permanent);
        return permanent;
    }

    private void addBlackMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private void addTsaboTavoc() {
        Permanent permanent = new Permanent(new TsaboTavoc());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
    }
}
