package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DressDown.class, ElvishMystic.class, GrizzlyBears.class})
class DressDownTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters the battlefield")
    void drawsCardOnEnter() {
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addDressDownMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures lose their abilities while it remains on the battlefield")
    void creaturesLoseAbilities() {
        Permanent mystic = addCreatureReady(player1, new ElvishMystic());
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addDressDownMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mystic)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of each end step")
    void sacrificesAtEndStep() {
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addDressDownMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dress Down");
        harness.assertInGraveyard(player1, "Dress Down");
    }

    private void addDressDownMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
