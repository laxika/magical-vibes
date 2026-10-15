package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoundingKrasis.class, GrizzlyBears.class, Forest.class})
class BoundingKrasisTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BoundingKrasis(), "{1}{G}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB taps an untapped target creature")
    void etbTapsUntappedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Bounding Krasis");
    }

    @Test
    @DisplayName("ETB untaps a tapped target creature")
    void etbUntapsTappedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();

        cast();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the may leaves the creature untouched")
    void decliningMayDoesNothing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bounding Krasis");
    }

    @Test
    @DisplayName("Only creatures are legal targets")
    void onlyCreaturesAreLegalTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(forest.getId());
    }

    @Test
    @DisplayName("Krasis itself is a legal target for its own trigger")
    void canTargetItself() {
        harness.addToBattlefield(player2, new Forest());

        cast();

        var krasisId = harness.getPermanentId(player1, "Bounding Krasis");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(krasisId);

        harness.handlePermanentChosen(player1, krasisId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Bounding Krasis").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat")
    void canCastDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castFromHand(player1, new BoundingKrasis(), "{1}{G}{U}");
        harness.passBothPriorities();

        var krasisId = harness.getPermanentId(player1, "Bounding Krasis");
        harness.handlePermanentChosen(player1, krasisId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Bounding Krasis");
        assertThat(findPermanent(player1, "Bounding Krasis").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB can untap another creature controlled by the Krasis controller")
    void canUntapOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BoundingKrasis());
        creature.tap();

        cast();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger does nothing when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BoundingKrasis());

        cast();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The trigger still resolves after Bounding Krasis leaves")
    void triggerResolvesWithoutSource() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BoundingKrasis());

        cast();
        harness.handlePermanentChosen(player1, creature.getId());
        Permanent source = findPermanent(player1, "Bounding Krasis");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Bounding Krasis");
        assertThat(gd.stack).isEmpty();
    }
}
