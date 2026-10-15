package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorEntanglers.class, ExpeditionEnvoy.class, SnappingGnarlid.class, Conspiracy.class})
class KorEntanglersTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry taps target creature an opponent controls")
    void ownAllyEntryTapsTargetCreature() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        castKorEntanglers();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry taps target creature an opponent controls")
    void anotherAllyEntryTapsTargetCreature() {
        harness.addToBattlefield(player1, new KorEntanglers());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        harness.castFromHand(player1, new ExpeditionEnvoy(), "{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger the tap ability")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new KorEntanglers());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        harness.castFromHand(player1, new SnappingGnarlid(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger cannot target a creature you control")
    void triggerCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new KorEntanglers());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        harness.addToBattlefield(player2, new SnappingGnarlid());
        harness.castFromHand(player1, new ExpeditionEnvoy(), "{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new KorEntanglers());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ExpeditionEnvoy(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedCreatureIsALegalTarget() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        victim.tap();
        castKorEntanglers();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noOpponentCreaturesLeavesNoPendingTargetChoice() {
        castKorEntanglers();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Kor Entanglers");
    }

    @Test
    @CardUsed(Conspiracy.class)
    void ownEntryTriggersEvenWhenItsAllySubtypeIsReplaced() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());

        castKorEntanglers();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    private void castKorEntanglers() {
        harness.castFromHand(player1, new KorEntanglers(), "{4}{W}");
    }
}
