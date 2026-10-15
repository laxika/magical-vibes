package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Improvise pays one generic mana by tapping an artifact without activating it")
    void improvisePaysOneGenericMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArcReactor());
        harness.setHand(player1, List.of(new ArcReactor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(source.getId()));

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Arc Reactor")).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Improvise cannot use an already tapped artifact")
    void improviseRejectsTappedArtifact() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArcReactor());
        source.tap();
        harness.setHand(player1, List.of(new ArcReactor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An artifact cannot pay twice for the same spell through improvise")
    void improviseRejectsRepeatedArtifact() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArcReactor());
        harness.setHand(player1, List.of(new ArcReactor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Arc Reactor's mana ability resolves immediately even when newly controlled")
    void manaAbilityDoesNotUseStackOrRequireHaste() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new ArcReactor());
        reactor.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(reactor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Arc Reactor cannot activate its tap ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new ArcReactor());
        reactor.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
