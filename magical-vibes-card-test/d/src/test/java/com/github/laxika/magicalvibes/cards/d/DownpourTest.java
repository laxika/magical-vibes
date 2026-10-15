package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
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

@CardUsed({Downpour.class, WalkingCorpse.class, VampireNighthawk.class, JayemdaeTome.class})
class DownpourTest extends BaseCardTest {

    @Test
    @DisplayName("Taps three target creatures")
    void tapsThreeTargetCreatures() {
        Permanent c1 = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent c2 = harness.addToBattlefieldAndReturn(player2, new VampireNighthawk());
        Permanent c3 = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(c1.getId(), c2.getId(), c3.getId()));

        assertThat(c1.isTapped()).isTrue();
        assertThat(c2.isTapped()).isTrue();
        assertThat(c3.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target just one creature")
    void canTargetJustOne() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Downpour");
    }

    @Test
    @DisplayName("Cannot target more than three creatures")
    void cannotTargetMoreThanThree() {
        Permanent c1 = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent c2 = harness.addToBattlefieldAndReturn(player2, new VampireNighthawk());
        Permanent c3 = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent c4 = harness.addToBattlefieldAndReturn(player2, new VampireNighthawk());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(c1.getId(), c2.getId(), c3.getId(), c4.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new JayemdaeTome());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID tomeId = harness.getPermanentId(player2, "Jayemdae Tome");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(tomeId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Skips targets that left the battlefield before resolution")
    void skipsRemovedTargets() {
        Permanent c1 = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent c2 = harness.addToBattlefieldAndReturn(player2, new VampireNighthawk());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(c1.getId(), c2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(c1);
        harness.passBothPriorities();

        assertThat(c2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can resolve with zero targets without tapping any creature")
    void canResolveWithZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Downpour");
    }

    @Test
    @DisplayName("Can target two creatures controlled by different players")
    void tapsCreaturesControlledByBothPlayers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new VampireNighthawk());
        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(own.getId(), opposing.getId()));

        assertThat(own.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Already tapped creatures are legal targets")
    void canTargetAlreadyTappedCreature() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        tapped.tap();
        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new VampireNighthawk());
        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(tapped.getId(), untapped.getId()));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Downpour");
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotRepeatTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Does not tap anything when all targets leave before resolution")
    void allTargetsLeavingPreventsResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new VampireNighthawk());
        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(other.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Downpour");
    }
}
