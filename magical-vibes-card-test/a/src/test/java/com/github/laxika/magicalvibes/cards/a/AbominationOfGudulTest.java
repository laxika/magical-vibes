package com.github.laxika.magicalvibes.cards.a;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SmokeTeller;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({AbominationOfGudul.class, SmokeTeller.class, Forest.class})
class AbominationOfGudulTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the combat-damage trigger draws a card, then discards a card")
    void acceptingTriggerDrawsThenDiscards() {
        harness.setHand(player1, new ArrayList<>(List.of(new SmokeTeller())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        addAttacker();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Smoke Teller");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the combat-damage trigger does not draw or discard")
    void decliningTriggerDoesNothing() {
        harness.setHand(player1, new ArrayList<>(List.of(new SmokeTeller())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        addAttacker();

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Smoke Teller");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new AbominationOfGudul());
        harness.addToBattlefield(player2, new SmokeTeller());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDiscardTheCardJustDrawnFromAnEmptyHand() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addAttacker();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void faceDownCombatDamageDoesNotTriggerLooting() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent attacker = castFaceDown();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void payingMorphCostRestoresCombatDamageTrigger() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent attacker = castFaceDown();
        assertThat(attacker.isFaceDown()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        assertThat(attacker.isFaceDown()).isFalse();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new AbominationOfGudul()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void addAttacker() {
        Permanent attacker = addCreatureReady(player1, new AbominationOfGudul());
        attacker.setAttacking(true);
    }
}
