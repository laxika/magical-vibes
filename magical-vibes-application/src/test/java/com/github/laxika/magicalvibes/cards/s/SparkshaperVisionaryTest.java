package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparkshaperVisionary.class, JaceBeleren.class, ChandraNalaar.class, Forest.class})
class SparkshaperVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Turns any number of your planeswalkers into temporary flying Birds")
    void transformsAnyNumberOfControlledPlaneswalkers() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent jace = addPlaneswalker(player1, new JaceBeleren(), 3);
        Permanent chandra = addPlaneswalker(player1, new ChandraNalaar(), 6);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, jace.getId());
        harness.handlePermanentChosen(player1, chandra.getId());
        harness.passBothPriorities();

        for (Permanent planeswalker : List.of(jace, chandra)) {
            assertThat(gqs.isCreature(gd, planeswalker)).isTrue();
            assertThat(gqs.isPlaneswalker(gd, planeswalker)).isFalse();
            assertThat(gqs.getEffectivePower(gd, planeswalker)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, planeswalker)).isEqualTo(3);
            assertThat(gqs.effectiveCreatureSubtypes(gd, planeswalker))
                    .containsExactly(CardSubtype.BIRD);
            assertThat(gqs.hasKeyword(gd, planeswalker, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, planeswalker, Keyword.HEXPROOF)).isTrue();
            assertThat(planeswalker.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER))
                    .hasSize(1);
        }

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gqs.isCreature(gd, jace)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, jace)).isTrue();
        assertThat(jace.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).isEmpty();
    }

    @Test
    @DisplayName("The granted ability scries when the animated planeswalker deals combat damage")
    void combatDamageTriggersScry() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent jace = addPlaneswalker(player1, new JaceBeleren(), 3);
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, jace.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Cannot choose an opponent's planeswalker")
    void cannotTargetOpponentPlaneswalker() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent ownJace = addPlaneswalker(player1, new JaceBeleren(), 3);
        Permanent opponentJace = addPlaneswalker(player2, new JaceBeleren(), 3);

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentJace.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, ownJace.getId());
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(Player player, Card card, int loyalty) {
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
