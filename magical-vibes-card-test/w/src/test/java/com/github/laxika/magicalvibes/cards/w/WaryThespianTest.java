package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaryThespian.class, GrizzlyBears.class, Shock.class})
class WaryThespianTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castFromHand(player1, new WaryThespian(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("When it dies, Wary Thespian surveils 1")
    void diesWithSurveil() {
        Permanent thespian = addReadyThespian(player1);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        killWithShock(player2, thespian.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Another creature's death does not trigger Wary Thespian")
    void anotherCreatureDeathDoesNotTrigger() {
        addReadyThespian(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(player1, bears.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering surveil may leave the top card in the library")
    void enteringSurveilCanKeepTopCard() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new WaryThespian(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death surveil may leave the top card in the library")
    void deathSurveilCanKeepTopCard() {
        Permanent thespian = addReadyThespian(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        killWithShock(player2, thespian.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(thespian.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering surveil with an empty library finishes without a choice")
    void enteringSurveilWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new WaryThespian(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death surveil with an empty library finishes without a choice")
    void deathSurveilWithEmptyLibrary() {
        Permanent thespian = addReadyThespian(player1);
        harness.setLibrary(player1, List.of());

        killWithShock(player2, thespian.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(thespian.getCard());
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyThespian(Player player) {
        Permanent thespian = harness.addToBattlefieldAndReturn(player, new WaryThespian());
        thespian.setSummoningSick(false);
        return thespian;
    }

    private void killWithShock(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
