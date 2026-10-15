package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplitUp.class, GrizzlyBears.class, Plains.class})
class SplitUpTest extends BaseCardTest {

    @Test
    @DisplayName("The tapped-creature mode destroys tapped creatures and leaves untapped creatures")
    void destroysTappedCreatures() {
        Permanent tappedOwnCreature = addCreature(player1);
        tappedOwnCreature.tap();
        Permanent untappedOwnCreature = addCreature(player1);
        Permanent tappedOpponentCreature = addCreature(player2);
        tappedOpponentCreature.tap();
        Permanent untappedOpponentCreature = addCreature(player2);

        castSplitUp(0);

        assertThat(battlefieldContains(player1, tappedOwnCreature)).isFalse();
        assertThat(battlefieldContains(player1, untappedOwnCreature)).isTrue();
        assertThat(battlefieldContains(player2, tappedOpponentCreature)).isFalse();
        assertThat(battlefieldContains(player2, untappedOpponentCreature)).isTrue();
    }

    @Test
    @DisplayName("The untapped-creature mode destroys untapped creatures and leaves tapped creatures")
    void destroysUntappedCreatures() {
        Permanent tappedOwnCreature = addCreature(player1);
        tappedOwnCreature.tap();
        Permanent untappedOwnCreature = addCreature(player1);
        Permanent tappedOpponentCreature = addCreature(player2);
        tappedOpponentCreature.tap();
        Permanent untappedOpponentCreature = addCreature(player2);

        castSplitUp(1);

        assertThat(battlefieldContains(player1, tappedOwnCreature)).isTrue();
        assertThat(battlefieldContains(player1, untappedOwnCreature)).isFalse();
        assertThat(battlefieldContains(player2, tappedOpponentCreature)).isTrue();
        assertThat(battlefieldContains(player2, untappedOpponentCreature)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void checksTappedStatusAtResolution(int mode) {
        Permanent initiallyMatching = addCreature(player1);
        Permanent initiallyNotMatching = addCreature(player2);
        if (mode == 0) {
            initiallyMatching.tap();
        } else {
            initiallyNotMatching.tap();
        }
        harness.setHand(player1, List.of(new SplitUp()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, mode);

        if (mode == 0) {
            initiallyMatching.untap();
            initiallyNotMatching.tap();
        } else {
            initiallyMatching.tap();
            initiallyNotMatching.untap();
        }
        harness.passBothPriorities();

        assertThat(battlefieldContains(player1, initiallyMatching)).isTrue();
        assertThat(battlefieldContains(player2, initiallyNotMatching)).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Split Up");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void leavesNoncreaturePermanentsAlone(int mode) {
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        tappedLand.tap();
        Permanent untappedLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent creature = addCreature(player2);
        if (mode == 0) {
            creature.tap();
        }

        castSplitUp(mode);

        assertThat(battlefieldContains(player1, tappedLand)).isTrue();
        assertThat(battlefieldContains(player2, untappedLand)).isTrue();
        assertThat(battlefieldContains(player2, creature)).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void resolvesWithNoMatchingCreatures(int mode) {
        Permanent creature = addCreature(player2);
        if (mode == 1) {
            creature.tap();
        }

        castSplitUp(mode);

        assertThat(battlefieldContains(player2, creature)).isTrue();
        harness.assertInGraveyard(player1, "Split Up");
        assertThat(gd.stack).isEmpty();
    }

    private void castSplitUp(int mode) {
        harness.setHand(player1, List.of(new SplitUp()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0, mode);
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private boolean battlefieldContains(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(candidate -> candidate.getId().equals(permanent.getId()));
    }
}
