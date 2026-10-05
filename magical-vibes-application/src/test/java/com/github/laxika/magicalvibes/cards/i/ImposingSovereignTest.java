package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.k.KalonianTusker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImposingSovereign.class, KalonianTusker.class, DarksteelIngot.class, DoomBlade.class})
class ImposingSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creatures enter tapped")
    void opponentsCreaturesEnterTapped() {
        harness.addToBattlefield(player1, new ImposingSovereign());
        harness.setHand(player2, List.of(new KalonianTusker()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player2, "Kalonian Tusker");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures do NOT enter tapped")
    void controllersCreaturesDoNotEnterTapped() {
        harness.addToBattlefield(player1, new ImposingSovereign());
        harness.setHand(player1, List.of(new KalonianTusker()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Kalonian Tusker");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures enter tapped without being cast, even while Sovereign is tapped")
    void noncastCreaturesEnterTappedWhileSovereignIsTapped() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new ImposingSovereign());
        sovereign.tap();

        Permanent creature = harness.enterBattlefieldAndReturn(player2, new KalonianTusker());

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's noncreature artifacts enter untapped")
    void opponentsNoncreatureArtifactsEnterUntapped() {
        harness.addToBattlefield(player1, new ImposingSovereign());

        Permanent artifact = harness.enterBattlefieldAndReturn(player2, new DarksteelIngot());

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sovereign does not tap creatures already on the battlefield")
    void existingCreaturesRemainUntapped() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KalonianTusker());

        Permanent sovereign = harness.enterBattlefieldAndReturn(player1, new ImposingSovereign());

        assertThat(creature.isTapped()).isFalse();
        assertThat(sovereign.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creatures enter untapped after Sovereign leaves the battlefield")
    void creaturesEnterUntappedAfterSovereignIsDestroyed() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new ImposingSovereign());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, sovereign.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sovereign);
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new KalonianTusker());
        assertThat(creature.isTapped()).isFalse();
    }
}
