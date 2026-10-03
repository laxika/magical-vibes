package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TitansStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArenaAthlete.class, GrizzlyBears.class, Shock.class, TitansStrength.class})
class ArenaAthleteTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic makes a chosen opponent's creature unable to block this turn")
    void heroicMakesOpponentCreatureUnableToBlock() {
        Permanent athlete = harness.addToBattlefieldAndReturn(player1, new ArenaAthlete());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, athlete.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Heroic cannot target a creature you control")
    void heroicCannotTargetOwnCreature() {
        Permanent athlete = harness.addToBattlefieldAndReturn(player1, new ArenaAthlete());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, athlete.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger heroic")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new ArenaAthlete());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's spell targeting Arena Athlete does not trigger heroic")
    void opponentSpellDoesNotTriggerHeroic() {
        Permanent athlete = harness.addToBattlefieldAndReturn(player1, new ArenaAthlete());
        harness.addToBattlefield(player2, new ArenaAthlete());
        harness.setHand(player2, List.of(new TitansStrength()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, athlete.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("With no opposing creature, heroic cannot be put on the stack")
    void noLegalHeroicTargetLeavesOnlyTheSpell() {
        Permanent athlete = harness.addToBattlefieldAndReturn(player1, new ArenaAthlete());
        harness.setHand(player1, List.of(new TitansStrength()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, athlete.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Heroic only triggers for the Arena Athlete actually targeted")
    void onlyTargetedAthleteTriggers() {
        Permanent targetedAthlete = harness.addToBattlefieldAndReturn(player1, new ArenaAthlete());
        harness.addToBattlefield(player1, new ArenaAthlete());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ArenaAthlete());
        harness.setHand(player1, List.of(new TitansStrength()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetedAthlete.getId());
        harness.handlePermanentChosen(player1, blocker.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Each spell targeting Arena Athlete triggers heroic independently")
    void repeatedSpellsDisableDifferentBlockers() {
        Permanent athlete = harness.addToBattlefieldAndReturn(player1, new ArenaAthlete());
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new ArenaAthlete());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new ArenaAthlete());
        harness.setHand(player1, List.of(new TitansStrength(), new TitansStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, athlete.getId());
        harness.handlePermanentChosen(player1, firstBlocker.getId());
        harness.passBothPriorities();
        assertThat(firstBlocker.isCantBlockThisTurn()).isTrue();
        assertThat(secondBlocker.isCantBlockThisTurn()).isFalse();

        harness.castInstant(player1, 0, athlete.getId());
        harness.handlePermanentChosen(player1, secondBlocker.getId());
        harness.passBothPriorities();

        assertThat(firstBlocker.isCantBlockThisTurn()).isTrue();
        assertThat(secondBlocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at the end of the turn")
    void blockingRestrictionExpires() {
        Permanent athlete = harness.addToBattlefieldAndReturn(player1, new ArenaAthlete());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, athlete.getId());
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Heroic resolves even if Arena Athlete is removed in response")
    void heroicResolvesAfterSourceIsRemoved() {
        Permanent athlete = harness.addToBattlefieldAndReturn(player1, new ArenaAthlete());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, athlete.getId());
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.castInstant(player2, 0, athlete.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Arena Athlete");
        assertThat(blocker.isCantBlockThisTurn()).isFalse();

        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }
}
