package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gloomwidow;
import com.github.laxika.magicalvibes.cards.i.InkfathomInfiltrator;
import com.github.laxika.magicalvibes.cards.w.WickerWarcrawler;
import com.github.laxika.magicalvibes.cards.z.ZealousGuardian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MassCalcify.class, Forest.class, Gloomwidow.class, WickerWarcrawler.class,
        ZealousGuardian.class, InkfathomInfiltrator.class})
class MassCalcifyTest extends BaseCardTest {

    private void castMassCalcify() {
        harness.setHand(player1, List.of(new MassCalcify()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Destroys all nonwhite creatures on both battlefields")
    void destroysNonwhiteCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Gloomwidow());
        harness.addToBattlefield(player2, new Gloomwidow());

        castMassCalcify();

        harness.assertNotOnBattlefield(player1, "Gloomwidow");
        harness.assertNotOnBattlefield(player2, "Gloomwidow");
    }

    @Test
    @DisplayName("Leaves white creatures on the battlefield")
    void leavesWhiteCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new ZealousGuardian());
        harness.addToBattlefield(player2, new ZealousGuardian());
        harness.addToBattlefield(player2, new Gloomwidow());

        castMassCalcify();

        harness.assertOnBattlefield(player1, "Zealous Guardian");
        harness.assertOnBattlefield(player2, "Zealous Guardian");
        harness.assertNotOnBattlefield(player2, "Gloomwidow");
    }

    @Test
    @DisplayName("Destroys colorless artifact creatures but leaves noncreature permanents")
    void destroysColorlessArtifactCreaturesButLeavesNoncreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new WickerWarcrawler());
        harness.addToBattlefield(player2, new Forest());

        castMassCalcify();

        harness.assertNotOnBattlefield(player1, "Wicker Warcrawler");
        harness.assertInGraveyard(player1, "Wicker Warcrawler");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Destroys multicolored creatures with no white color")
    void destroysNonwhiteMulticoloredCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new InkfathomInfiltrator());
        harness.addToBattlefield(player2, new InkfathomInfiltrator());
        harness.addToBattlefield(player2, new ZealousGuardian());

        castMassCalcify();

        harness.assertNotOnBattlefield(player1, "Inkfathom Infiltrator");
        harness.assertNotOnBattlefield(player2, "Inkfathom Infiltrator");
        harness.assertInGraveyard(player1, "Inkfathom Infiltrator");
        harness.assertInGraveyard(player2, "Inkfathom Infiltrator");
        harness.assertOnBattlefield(player2, "Zealous Guardian");
    }

    @Test
    @DisplayName("Resolves without any creatures on the battlefield")
    void resolvesOnEmptyBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castMassCalcify();

        harness.assertInGraveyard(player1, "Mass Calcify");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }
}
