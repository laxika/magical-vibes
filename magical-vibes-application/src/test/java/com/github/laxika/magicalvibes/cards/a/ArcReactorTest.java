package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ArcReactor.class)
class ArcReactorTest extends BaseCardTest {

    @Test
    @DisplayName("Arc Reactor enters the battlefield tapped")
    void entersTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new ArcReactor()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Arc Reactor").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Arc Reactor adds three colorless mana")
    void tappingAddsThreeColorlessMana() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new ArcReactor());
        reactor.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(reactor.isTapped()).isTrue();
    }
}
