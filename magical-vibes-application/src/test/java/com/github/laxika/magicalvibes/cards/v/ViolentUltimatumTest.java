package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlgaeGharial;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfJund;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViolentUltimatum.class, GrizzlyBears.class, HillGiant.class, Forest.class,
        ObeliskOfJund.class, Unsummon.class, DrudgeSkeletons.class, AlgaeGharial.class})
class ViolentUltimatumTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.RED, 3);
        harness.addMana(player, ManaColor.GREEN, 2);
    }

    @Test
    @DisplayName("Destroys three target permanents")
    void destroysThreePermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);

        List<UUID> targets = gd.playerBattlefields.get(player2.getId()).stream()
                .map(p -> p.getId()).toList();

        harness.castAndResolveSorcery(player1, 0, targets);

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Must target exactly three permanents")
    void mustTargetThree() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);

        List<UUID> twoTargets = gd.playerBattlefields.get(player2.getId()).stream()
                .map(p -> p.getId()).toList();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, twoTargets))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target");
    }

    @Test
    @DisplayName("Can destroy own permanents, artifacts, and lands")
    void destroysMixedPermanentTypesAndControllers() {
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        var obelisk = harness.addToBattlefieldAndReturn(player2, new ObeliskOfJund());
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var survivor = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0,
                List.of(forest.getId(), obelisk.getId(), bears.getId()));

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Obelisk of Jund");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        harness.assertInGraveyard(player1, "Violent Ultimatum");
    }

    @Test
    @DisplayName("Cannot choose the same permanent twice")
    void rejectsDuplicateTargets() {
        var first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
        harness.assertInHand(player1, "Violent Ultimatum");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Cannot choose four permanents")
    void rejectsTooManyTargets() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);
        List<UUID> targets = gd.playerBattlefields.get(player2.getId()).stream()
                .map(p -> p.getId()).toList();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target");
        harness.assertInHand(player1, "Violent Ultimatum");
    }

    @Test
    @DisplayName("Destroys the remaining legal targets when one target leaves")
    void resolvesWithOneTargetMissing() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        var forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, List.of(bears.getId(), giant.getId(), forest.getId()));

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Violent Ultimatum");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not resolve when all three targets leave")
    void doesNotResolveWithAllTargetsMissing() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        var survivor = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        List<UUID> targets = gd.playerBattlefields.get(player2.getId()).stream()
                .limit(3).map(p -> p.getId()).toList();
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        harness.setHand(player2, List.of(new Unsummon(), new Unsummon(), new Unsummon()));
        addMana(player1);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, targets);

        for (UUID target : targets) {
            harness.castAndResolveInstant(player2, 0, target);
        }
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        harness.assertInGraveyard(player1, "Violent Ultimatum");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("fizzles"));
    }

    @Test
    @DisplayName("Regeneration saves one target without saving the other two")
    void allowsRegeneration() {
        var skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, List.of(skeletons.getId(), bears.getId(), giant.getId()));

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(skeletons);
        assertThat(skeletons.isTapped()).isTrue();
        harness.assertNotInGraveyard(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Violent Ultimatum");
    }

    @Test
    @DisplayName("Cannot target a permanent with shroud")
    void rejectsShroudTarget() {
        var gharial = harness.addToBattlefieldAndReturn(player2, new AlgaeGharial());
        var forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        var obelisk = harness.addToBattlefieldAndReturn(player2, new ObeliskOfJund());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(gharial.getId(), forest.getId(), obelisk.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
        harness.assertInHand(player1, "Violent Ultimatum");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(gharial, forest, obelisk);
    }

    @Test
    @DisplayName("Cannot choose a player as one of the three targets")
    void rejectsPlayerTarget() {
        var forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        var obelisk = harness.addToBattlefieldAndReturn(player2, new ObeliskOfJund());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(forest.getId(), obelisk.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Violent Ultimatum");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(forest, obelisk);
    }
}
