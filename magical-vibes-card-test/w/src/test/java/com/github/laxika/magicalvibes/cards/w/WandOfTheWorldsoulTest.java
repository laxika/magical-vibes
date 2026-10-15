package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WandOfTheWorldsoul.class, GrizzlyBears.class, SolRing.class})
class WandOfTheWorldsoulTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for white mana")
    void entersTappedAndTapsForWhiteMana() {
        harness.setHand(player1, List.of(new WandOfTheWorldsoul()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent wand = findPermanent(player1, "Wand of the Worldsoul");
        assertThat(wand.isTapped()).isTrue();

        wand.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives the next spell convoke and consumes the grant")
    void grantsConvokeToNextSpell() {
        harness.addToBattlefield(player1, new WandOfTheWorldsoul());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        SolRing firstSolRing = new SolRing();
        harness.setHand(player1, List.of(firstSolRing));
        UUID creatureId = creature.getId();
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creatureId));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        creature.untap();
        harness.setHand(player1, List.of(new SolRing()));
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Convoke is granted only after the activated ability resolves")
    void grantRequiresResolution() {
        harness.addToBattlefield(player1, new WandOfTheWorldsoul());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SolRing()));

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(1);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);

        harness.passBothPriorities();
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    @DisplayName("Casting without tapping creatures still consumes the convoke grant")
    void castingWithoutConvokeConsumesGrant() {
        harness.addToBattlefield(player1, new WandOfTheWorldsoul());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SolRing(), new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two activations both apply to the same next spell")
    void multipleGrantsAreConsumedBySameSpell() {
        harness.addToBattlefield(player1, new WandOfTheWorldsoul());
        harness.addToBattlefield(player1, new WandOfTheWorldsoul());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SolRing(), new SolRing()));
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        creature.untap();
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unused convoke grant expires at the end of the turn")
    void unusedGrantExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new WandOfTheWorldsoul());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(new SolRing()));
        harness.setLibrary(player1, List.of(new SolRing()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SolRing()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Granted convoke pays both colored and generic mana with newly entered creatures")
    void convokePaysColoredAndGenericMana() {
        harness.addToBattlefield(player1, new WandOfTheWorldsoul());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(first.getId(), second.getId()));
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }
}
