package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.g.GreatFurnace;
import com.github.laxika.magicalvibes.cards.s.SeedbornMuse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlindingBeam.class, AlphaMyr.class, GreatFurnace.class, SeedbornMuse.class})
class BlindingBeamTest extends BaseCardTest {

    @Test
    @DisplayName("Tap mode taps exactly two target creatures")
    void tapModeTapsTwoTargetCreatures() {
        Permanent first = addCreatureReady(player2, new AlphaMyr());
        Permanent second = addCreatureReady(player2, new AlphaMyr());
        Permanent third = addCreatureReady(player2, new AlphaMyr());
        cast(player1, new int[]{0}, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap mode prevents target player's creatures from untapping next turn")
    void untapModePreventsNextUntap() {
        Permanent targetCreature = addCreatureReady(player2, new AlphaMyr());
        targetCreature.tap();
        Permanent otherCreature = addCreatureReady(player2, new AlphaMyr());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GreatFurnace());
        artifact.tap();

        cast(player1, new int[]{1}, List.of(player2.getId()));

        assertThat(targetCreature.getSkipUntapCount()).isEqualTo(1);
        assertThat(otherCreature.getSkipUntapCount()).isEqualTo(1);
        assertThat(artifact.getSkipUntapCount()).isZero();

        advanceToUpkeep(player2);

        assertThat(targetCreature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap mode can target the caster")
    void untapModeCanTargetController() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        creature.tap();

        cast(player1, new int[]{1}, List.of(player1.getId()));

        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Untap mode affects creatures entering before the target player's next untap")
    void untapModeAffectsCreaturesEnteringBeforeNextUntap() {
        cast(player1, new int[]{1}, List.of(player2.getId()));

        Permanent lateCreature = addCreatureReady(player2, new AlphaMyr());
        lateCreature.tap();

        advanceToUpkeep(player2);

        assertThat(lateCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untap mode affects creatures that untap through another player's untap effect")
    void untapModeAffectsOtherPlayersCreaturesDuringTargetPlayersUntap() {
        harness.addToBattlefieldAndReturn(player1, new SeedbornMuse());
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        creature.tap();

        cast(player1, new int[]{1}, List.of(player2.getId()));
        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entwine pays one extra mana and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent first = addCreatureReady(player2, new AlphaMyr());
        Permanent second = addCreatureReady(player2, new AlphaMyr());
        Permanent third = addCreatureReady(player2, new AlphaMyr());
        cast(player1, new int[]{0, 1}, List.of(first.getId(), second.getId(), player2.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isFalse();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
        assertThat(third.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Tap mode rejects a noncreature target")
    void tapModeRejectsNoncreatureTarget() {
        Permanent creature = addCreatureReady(player2, new AlphaMyr());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GreatFurnace());
        harness.setHand(player1, List.of(new BlindingBeam()));
        addMana(player1, false);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untap mode rejects a permanent target")
    void untapModeRejectsPermanentTarget() {
        Permanent creature = addCreatureReady(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new BlindingBeam()));
        addMana(player1, false);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entwine is rejected without its additional mana")
    void entwineRequiresAdditionalMana() {
        Permanent first = addCreatureReady(player2, new AlphaMyr());
        Permanent second = addCreatureReady(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new BlindingBeam()));
        addMana(player1, false);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1},
                List.of(first.getId(), second.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Player player, int[] modes, List<UUID> targetIds) {
        harness.setHand(player, List.of(new BlindingBeam()));
        addMana(player, modes.length == 2);
        harness.castModalInstantWithModes(player, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana(Player player, boolean entwined) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        if (entwined) {
            harness.addMana(player, ManaColor.COLORLESS, 1);
        }
    }

}
