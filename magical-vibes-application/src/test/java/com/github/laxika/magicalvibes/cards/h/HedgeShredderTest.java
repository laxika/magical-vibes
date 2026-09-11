package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HedgeShredder.class, Forest.class, GrizzlyBears.class})
class HedgeShredderTest extends BaseCardTest {

    @Test
    @DisplayName("Crew 1 animates Hedge Shredder and taps the chosen creature")
    void crewAnimatesVehicle() {
        Permanent shredder = addReadyShredder();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shredder.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking may mill two cards")
    void attackingMayMillTwoCards() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        crewShredder();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Declining the attack trigger does not mill")
    void decliningAttackTriggerDoesNotMill() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        crewShredder();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands milled in one event return to the battlefield tapped")
    void milledLandsReturnTapped() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        crewShredder();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only lands milled by Hedge Shredder return")
    void onlyMilledLandsReturn() {
        Card forest = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, nonland));
        crewShredder();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
    }

    private Permanent addReadyShredder() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new HedgeShredder());
        shredder.setSummoningSick(false);
        return shredder;
    }

    private Permanent crewShredder() {
        Permanent shredder = addReadyShredder();
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return shredder;
    }
}
