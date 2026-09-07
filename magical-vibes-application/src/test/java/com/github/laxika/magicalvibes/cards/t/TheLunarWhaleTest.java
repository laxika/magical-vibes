package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheLunarWhale.class, Forest.class, GrizzlyBears.class, Opt.class})
class TheLunarWhaleTest extends BaseCardTest {

    @Test
    void cannotPlayFromTopBeforeItAttacks() {
        addWhaleReady();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void attackingLetsControllerPlayLandAndCastSpellFromTop() {
        Permanent whale = addWhaleReady();
        Permanent crew = addCreatureReady(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, whale)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        Forest forest = new Forest();
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(forest, opt));

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Opt");
    }

    private Permanent addWhaleReady() {
        Permanent whale = harness.addToBattlefieldAndReturn(player1, new TheLunarWhale());
        whale.setSummoningSick(false);
        return whale;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
