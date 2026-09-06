package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleeperDart.class, GrizzlyBears.class, Forest.class})
class SleeperDartTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void entersDrawsCard() {
        harness.setHand(player1, List.of(new SleeperDart()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Activating the ability sacrifices the dart and skips the target creature's next untap")
    void abilitySacrificesAndSkipsNextUntap() {
        harness.addToBattlefield(player1, new SleeperDart());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sleeper Dart");
        harness.assertInGraveyard(player1, "Sleeper Dart");

        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isTrue();

        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new SleeperDart());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
