package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.cards.e.EmmessiTome;
import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.f.FlowstoneSculpture;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Legerdemain.class, FightingDrake.class, WindDrake.class, CursedScroll.class,
        EmmessiTome.class, FlowstoneSculpture.class})
class LegerdemainTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new Legerdemain()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Exchanges control of two creatures")
    void exchangesCreatures() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new FightingDrake());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Fighting Drake");
        harness.assertNotOnBattlefield(player1, "Fighting Drake");
        harness.assertOnBattlefield(player1, "Wind Drake");
        harness.assertNotOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Exchanges control of two artifacts")
    void exchangesArtifacts() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CursedScroll());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new EmmessiTome());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Cursed Scroll");
        harness.assertOnBattlefield(player1, "Emmessi Tome");
    }

    @Test
    @DisplayName("Exchanges control of a creature and an artifact creature")
    void exchangesCreatureAndArtifactCreature() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new FightingDrake());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new FlowstoneSculpture());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Fighting Drake");
        harness.assertOnBattlefield(player1, "Flowstone Sculpture");
    }

    @Test
    @DisplayName("Exchanges control of an artifact and an artifact creature")
    void exchangesArtifactAndArtifactCreature() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CursedScroll());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new FlowstoneSculpture());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Cursed Scroll");
        harness.assertOnBattlefield(player1, "Flowstone Sculpture");
    }

    @Test
    @DisplayName("Exchanges control when the opponent's permanent is the first target")
    void exchangesWithOpponentPermanentFirst() {
        prepare();
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new FightingDrake());

        harness.castAndResolveSorcery(player1, 0, List.of(opponents.getId(), own.getId()));

        harness.assertOnBattlefield(player1, "Wind Drake");
        harness.assertOnBattlefield(player2, "Fighting Drake");
    }

    @Test
    @DisplayName("Does nothing when both targets have the same controller (CR 701.12b)")
    void doesNothingWhenSameController() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FightingDrake());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WindDrake());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player1, "Fighting Drake");
        harness.assertOnBattlefield(player1, "Wind Drake");
        harness.assertNotOnBattlefield(player2, "Fighting Drake");
        harness.assertNotOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Exchange does nothing when a target leaves the battlefield before resolution (CR 701.12a)")
    void fizzlesWhenTargetGone() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new FightingDrake());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        harness.castSorcery(player1, 0, List.of(own.getId(), opponents.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponents);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fighting Drake");
        harness.assertNotOnBattlefield(player2, "Fighting Drake");
    }

    @Test
    @DisplayName("Cannot choose the same permanent for both targets")
    void cannotChooseSamePermanentTwice() {
        prepare();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new FightingDrake());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(permanent.getId(), permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pair a creature with a noncreature artifact — they share no type")
    void cannotPairCreatureWithArtifact() {
        prepare();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FightingDrake());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedScroll());

        UUID creatureId = creature.getId();
        UUID artifactId = artifact.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creatureId, artifactId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature type");
    }

    @Test
    @DisplayName("Offers only a second target that shares an artifact or creature type")
    void offersOnlySecondTargetsWithSharedType() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FightingDrake());
        Permanent matching = harness.addToBattlefieldAndReturn(player2, new FlowstoneSculpture());
        Permanent nonMatching = harness.addToBattlefieldAndReturn(player2, new CursedScroll());

        ValidTargetsResponse response = harness.getValidTargetService().computeValidTargetsForSpell(
                gd, gd.playerHands.get(player1.getId()).getFirst(), player1.getId(), List.of(first.getId()));

        assertThat(response.validPermanentIds()).containsExactly(matching.getId())
                .doesNotContain(nonMatching.getId());
    }
}
