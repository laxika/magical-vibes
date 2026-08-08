package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HerosDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Hero's Demise destroys the target legendary creature")
    void resolvingDestroysLegendaryCreature() {
        Permanent arvad = new Permanent(new ArvadTheCursed());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(arvad);

        harness.setHand(player1, List.of(new HerosDemise()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, arvad.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Arvad the Cursed");
        harness.assertInGraveyard(player2, "Arvad the Cursed");
        harness.assertInGraveyard(player1, "Hero's Demise");
    }

    @Test
    @DisplayName("Cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new ArvadTheCursed()));

        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new HerosDemise()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }
}
