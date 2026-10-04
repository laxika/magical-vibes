package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.MoonriseIntruder;
import com.github.laxika.magicalvibes.cards.v.VillageMessenger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzledAngler.class, GrislyAnglerfish.class, Cancel.class, Memnite.class, GrizzlyBears.class,
        VillageMessenger.class, MoonriseIntruder.class})
class GrizzledAnglerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability mills two and does not transform without a colorless creature in the graveyard")
    void millsWithoutTransformWhenNoColorlessCreature() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        harness.setLibrary(player1, List.of(new Cancel(), new Cancel()));

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(angler.isTransformed()).isFalse();
        assertThat(angler.getCard().getName()).isEqualTo("Grizzled Angler");
    }

    @Test
    @DisplayName("Tap ability transforms when a colorless creature is already in the graveyard")
    void transformsWhenColorlessCreatureAlreadyInGraveyard() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        harness.setGraveyard(player1, List.of(new Memnite()));
        harness.setLibrary(player1, List.of(new Cancel(), new Cancel()));

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isTrue();
        assertThat(angler.getCard().getName()).isEqualTo("Grisly Anglerfish");
        assertThat(gqs.getEffectivePower(gd, angler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angler)).isEqualTo(5);
    }

    @Test
    @DisplayName("Tap ability transforms when milling a colorless creature")
    void transformsWhenMillingColorlessCreature() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        harness.setLibrary(player1, List.of(new Memnite(), new Cancel()));

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isTrue();
        assertThat(angler.getCard().getName()).isEqualTo("Grisly Anglerfish");
    }

    @Test
    @DisplayName("Back face {6} forces opponents' creatures to attack this turn")
    void backFaceForcesOpponentCreaturesToAttack() {
        Permanent anglerfish = createTransformedAnglerfish(player1);
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, indexOf(player1, anglerfish), null, null);
        harness.passBothPriorities();

        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
        assertThat(ownBear.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void doesNotTransformForColoredCreatureInGraveyard() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        harness.setGraveyard(player1, List.of(new GrizzledAngler()));
        harness.setLibrary(player1, List.of(new Cancel(), new Cancel()));

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void doesNotTransformForOpponentsColorlessCreature() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        harness.setGraveyard(player2, List.of(new Memnite()));
        harness.setLibrary(player1, List.of(new Cancel(), new Cancel()));

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void transformsWithEmptyLibraryAndExistingColorlessCreature() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        harness.setGraveyard(player1, List.of(new Memnite()));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isTrue();
        assertThat(angler.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void checksGraveyardAtResolutionRatherThanActivation() {
        Permanent angler = addCreatureReady(player1, new GrizzledAngler());
        harness.setGraveyard(player1, List.of(new Memnite()));
        harness.setLibrary(player1, List.of(new Cancel(), new Cancel()));

        harness.activateAbility(player1, indexOf(player1, angler), null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void backFaceForcesHastyCreatureEnteringAfterResolutionToAttack() {
        Permanent anglerfish = createTransformedAnglerfish(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, indexOf(player1, anglerfish), null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new VillageMessenger());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId()))
                .contains(0);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent createTransformedAnglerfish(Player player) {
        Permanent angler = addCreatureReady(player, new GrizzledAngler());
        harness.setGraveyard(player, List.of(new Memnite()));
        harness.setLibrary(player, List.of(new Cancel(), new Cancel()));

        harness.activateAbility(player, indexOf(player, angler), null, null);
        harness.passBothPriorities();

        assertThat(angler.isTransformed()).isTrue();
        return angler;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
