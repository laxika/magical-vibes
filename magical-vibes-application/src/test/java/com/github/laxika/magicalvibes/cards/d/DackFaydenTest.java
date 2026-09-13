package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetedPermanentsOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DackFayden.class, GiantGrowth.class, GoldMyr.class})
class DackFaydenTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws two cards, then makes the target player discard two cards")
    void plusOneDrawsAndDiscards() {
        Permanent dack = addReadyDack(player1, 3);
        harness.setHand(player2, List.of(new DackFayden(), new DackFayden()));
        harness.setLibrary(player2, List.of(new DackFayden(), new DackFayden()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(dack.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-2 gains permanent control of target artifact")
    void minusTwoGainsControlOfArtifact() {
        Permanent dack = addReadyDack(player1, 3);
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new GoldMyr());

        harness.activateAbility(player1, 0, 1, null, myr.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, myr.getId())).isEqualTo(player1.getId());
        assertThat(dack.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-6 creates an emblem that gains control of permanents targeted by your spells")
    void minusSixCreatesTargetedPermanentControlEmblem() {
        Permanent dack = addReadyDack(player1, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).singleElement().satisfies(emblem -> {
            assertThat(emblem.controllerId()).isEqualTo(player1.getId());
            assertThat(emblem.staticEffects()).singleElement()
                    .isInstanceOf(GainControlOfTargetedPermanentsOnControllerSpellCastEffect.class);
        });
        assertThat(dack.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }

    @Test
    @DisplayName("Emblem gains control of every permanent targeted by a spell")
    void emblemGainsControlOfTargetedPermanent() {
        gd.emblems.add(new Emblem(player1.getId(),
                List.of(new GainControlOfTargetedPermanentsOnControllerSpellCastEffect()),
                new DackFayden()));
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new GoldMyr());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, myr.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, myr.getId())).isEqualTo(player1.getId());
    }

    private Permanent addReadyDack(Player player, int loyalty) {
        Permanent permanent = new Permanent(new DackFayden());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
