package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Onslaught.class, RagingGoblin.class, Spellbook.class})
class OnslaughtTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell triggers target selection")
    void creatureSpellTriggersTargetSelection() {
        harness.addToBattlefield(player1, new Onslaught());
        harness.addToBattlefield(player2, new RagingGoblin());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("The triggered ability taps the chosen creature")
    void tapsChosenCreature() {
        harness.addToBattlefield(player1, new Onslaught());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The trigger can tap a creature controlled by Onslaught's controller")
    void tapsCreatureControlledByOnslaughtController() {
        harness.addToBattlefield(player1, new Onslaught());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger the ability")
    void noncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Onslaught());

        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("A creature spell cast by an opponent does not trigger the ability")
    void opponentCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Onslaught());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new RagingGoblin(), "{R}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The trigger is skipped when no creature is available")
    void triggerSkippedWithoutCreatureTarget() {
        harness.addToBattlefield(player1, new Onslaught());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("A noncreature permanent cannot be chosen as the target")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Onslaught());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The trigger resolves before the creature spell")
    void triggerResolvesBeforeCreatureSpell() {
        harness.addToBattlefield(player1, new Onslaught());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void canTargetTappedCreature() {
        harness.addToBattlefield(player1, new Onslaught());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        target.tap();

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A creature entering without being cast does not trigger Onslaught")
    void creatureEnteringWithoutCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new Onslaught());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        harness.enterBattlefieldAndReturn(player1, new RagingGoblin());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger still resolves after Onslaught leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Onslaught());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }
}
