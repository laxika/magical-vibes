package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MassOfMysteries.class, AirElemental.class, GrizzlyBears.class})
class MassOfMysteriesTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat targets another Elemental creature you control")
    void beginningOfCombatTargetsAnotherElementalYouControl() {
        Permanent mass = addReadyCreature(player1, new MassOfMysteries());
        Permanent elemental = addReadyCreature(player1, new AirElemental());
        Permanent opponentElemental = addReadyCreature(player2, new AirElemental());
        Permanent nonElemental = addReadyCreature(player1, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(elemental.getId())
                .doesNotContain(mass.getId(), opponentElemental.getId(), nonElemental.getId());
    }

    @Test
    @DisplayName("Myriad creates one tapped attacking copy for each other opponent and exiles it at end of combat")
    void myriadCreatesAndExilesCopyAtEndOfCombat() {
        UUID thirdPlayerId = addThirdPlayer();
        addReadyCreature(player1, new MassOfMysteries());
        Permanent elemental = addReadyCreature(player1, new AirElemental());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1), Map.of(1, player2.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        List<Permanent> copies = findPermanents(player1, "Air Elemental").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(1);
        Permanent copy = copies.getFirst();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttackedThisTurn()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(thirdPlayerId);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Air Elemental").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private UUID addThirdPlayer() {
        UUID id = UUID.randomUUID();
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Carol");
        gd.playerIdToName.put(id, "Carol");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        gd.playerGraveyards.put(id, new ArrayList<>());
        return id;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
