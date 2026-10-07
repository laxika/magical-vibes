package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerashisCry.class, LanternKami.class, Plains.class})
class TerashisCryTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to three target creatures")
    void tapsThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        cast(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot choose more than three target creatures")
    void rejectsMoreThanThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        prepareCard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May tap fewer than three creatures")
    void tapsOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        cast(List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May choose no targets")
    void tapsNoCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        cast(List.of());

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can tap creatures controlled by either player")
    void tapsCreaturesControlledByEitherPlayer() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LanternKami());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        cast(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can resolve with no creatures on the battlefield")
    void resolvesWithNoCreatures() {
        cast(List.of());

        harness.assertInGraveyard(player1, "Terashi's Cry");
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void rejectsDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        prepareCard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Already tapped creatures remain legal targets")
    void canTargetTappedCreature() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        tapped.tap();

        cast(List.of(tapped.getId(), untapped.getId()));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Terashi's Cry");
    }

    @Test
    @DisplayName("Still taps remaining targets when one creature leaves the battlefield")
    void resolvesWithOneTargetGone() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        prepareCard();
        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        gd.playerGraveyards.get(player2.getId()).add(removed.getCard());

        harness.passBothPriorities();

        assertThat(removed.isTapped()).isFalse();
        assertThat(remaining.isTapped()).isTrue();
        assertThat(unchosen.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Terashi's Cry");
    }

    @Test
    @DisplayName("Does not affect unchosen creatures when all targets leave the battlefield")
    void allTargetsGone() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        prepareCard();
        harness.castSorcery(player1, 0, List.of(removed.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        gd.playerGraveyards.get(player2.getId()).add(removed.getCard());

        harness.passBothPriorities();

        assertThat(removed.isTapped()).isFalse();
        assertThat(unchosen.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Terashi's Cry");
    }

    private void cast(List<UUID> targets) {
        prepareCard();
        harness.castAndResolveSorcery(player1, 0, targets);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new TerashisCry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
