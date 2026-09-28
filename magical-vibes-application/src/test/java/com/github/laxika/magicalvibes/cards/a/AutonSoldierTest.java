package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AutonSoldier.class, ChoMannoRevolutionary.class, GrizzlyBears.class})
class AutonSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Copying a creature adds artifact and myriad and removes legendary")
    void copyingCreatureAppliesCopyExceptions() {
        Permanent auton = castAndCopy(new ChoMannoRevolutionary());

        assertThat(auton.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(auton.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(auton.getCard().getPower()).isEqualTo(2);
        assertThat(auton.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copied creature's myriad creates an attacking token copy")
    void copiedCreatureHasMyriad() {
        addThirdPlayer();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent auton = castAndCopy(new GrizzlyBears());
        auton.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(auton)));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player3.getId());
    }

    private Permanent castAndCopy(Card target) {
        harness.castFromHand(player1, new AutonSoldier(), "{4}{U}{U}");
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, target);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetPermanent.getId());
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Auton Soldier"))
                .findFirst()
                .orElseThrow();
    }

    private Player player3;

    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
    }
}
