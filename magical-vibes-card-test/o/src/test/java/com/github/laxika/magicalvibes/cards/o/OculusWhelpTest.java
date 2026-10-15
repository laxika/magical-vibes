package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.d.DaybreakRanger;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OculusWhelp.class, DaybreakRanger.class, Shock.class, BlasphemousAct.class})
class OculusWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it dies while you control a transformed permanent")
    void drawsWhenItDiesWithTransformedPermanent() {
        Permanent whelp = addCreatureReady(player1, new OculusWhelp());
        addTransformedPermanent();
        harness.setLibrary(player1, List.of(new DaybreakRanger()));

        killWithShock(whelp.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when it dies without a transformed permanent")
    void doesNotDrawWithoutTransformedPermanent() {
        Permanent whelp = addCreatureReady(player1, new OculusWhelp());
        harness.setLibrary(player1, List.of(new DaybreakRanger()));

        killWithShock(whelp.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's transformed permanent does not grant the death trigger")
    void doesNotDrawForOpponentsTransformedPermanent() {
        Permanent whelp = addCreatureReady(player1, new OculusWhelp());
        Permanent ranger = addCreatureReady(player2, new DaybreakRanger());
        ranger.setCard(ranger.getOriginalCard().getBackFaceCard());
        ranger.setTransformed(true);
        harness.setLibrary(player1, List.of(new OculusWhelp()));

        killWithShock(whelp.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A double-faced permanent on its front face does not grant the death trigger")
    void doesNotDrawForUntransformedPermanent() {
        Permanent whelp = addCreatureReady(player1, new OculusWhelp());
        addCreatureReady(player1, new DaybreakRanger());
        harness.setLibrary(player1, List.of(new OculusWhelp()));

        killWithShock(whelp.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple transformed permanents still grant only one draw")
    void drawsOnlyOnceWithMultipleTransformedPermanents() {
        Permanent whelp = addCreatureReady(player1, new OculusWhelp());
        addTransformedPermanent();
        addTransformedPermanent();
        harness.setLibrary(player1, List.of(new OculusWhelp(), new OculusWhelp()));

        killWithShock(whelp.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when the transformed permanent dies simultaneously, even if removed first")
    void drawsWhenTransformedPermanentDiesSimultaneously() {
        addTransformedPermanent();
        addCreatureReady(player1, new OculusWhelp());
        harness.setLibrary(player1, List.of(new OculusWhelp()));
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof OculusWhelp)
                .anyMatch(card -> card instanceof DaybreakRanger);
    }

    @Test
    @DisplayName("The draw still resolves after the last transformed permanent leaves")
    void drawsAfterTransformedPermanentLeavesBeforeTriggerResolves() {
        Permanent whelp = addCreatureReady(player1, new OculusWhelp());
        Permanent ranger = addCreatureReady(player1, new DaybreakRanger());
        ranger.setCard(ranger.getOriginalCard().getBackFaceCard());
        ranger.setTransformed(true);
        harness.setLibrary(player1, List.of(new OculusWhelp()));
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, whelp.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, ranger.getId());
        harness.castAndResolveInstant(player1, 0, ranger.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addTransformedPermanent() {
        Permanent ranger = addCreatureReady(player1, new DaybreakRanger());
        ranger.setCard(ranger.getOriginalCard().getBackFaceCard());
        ranger.setTransformed(true);
    }

    private void killWithShock(UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
