package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.Armageddon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        Equinox.class, Forest.class, StoneRain.class, Armageddon.class,
        Naturalize.class, GrizzlyBears.class, HolyStrength.class
})
class EquinoxTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell that would destroy a land you control")
    void countersTargetedLandDestruction() {
        Permanent forest = attachToForest();
        StoneRain stoneRain = new StoneRain();
        harness.setHand(player2, List.of(stoneRain));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, forest.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, stoneRain.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stone Rain");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not counter a spell that destroys a nonland permanent")
    void doesNotCounterNonlandDestruction() {
        Permanent forest = attachToForest();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getLast();
        Permanent holyStrength = new Permanent(new HolyStrength());
        holyStrength.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(holyStrength);

        Naturalize naturalize = new Naturalize();
        harness.setHand(player2, List.of(naturalize));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player2, 0, holyStrength.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, naturalize.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Naturalize");
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters a spell that would destroy all lands")
    void countersLandBoardWipe() {
        Permanent forest = attachToForest();
        Armageddon armageddon = new Armageddon();
        harness.setHand(player2, List.of(armageddon));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, armageddon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Armageddon");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    private Permanent attachToForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent equinox = new Permanent(new Equinox());
        equinox.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(equinox);
        return forest;
    }
}
