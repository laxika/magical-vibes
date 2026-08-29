package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfessorOnyx.class, BarkshellBlessing.class, GiantGrowth.class, GrizzlyBears.class,
        HillGiant.class, SerraAngel.class})
class ProfessorOnyxTest extends BaseCardTest {

    @Test
    @DisplayName("Magecraft drains each opponent when you cast an instant")
    void magecraftTriggersOnCast() {
        addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Magecraft triggers for each copied instant")
    void magecraftTriggersOnCopy() {
        addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("+1 loses life and puts one of the top three cards into hand")
    void plusOneLooksAtTopThree() {
        addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        Card chosen = new GiantGrowth();
        Card graveyardCard = new GrizzlyBears();
        Card otherGraveyardCard = new BarkshellBlessing();
        harness.setLibrary(player1, List.of(chosen, graveyardCard, otherGraveyardCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(graveyardCard, otherGraveyardCard);
    }

    @Test
    @DisplayName("-3 sacrifices each opponent's greatest-power creature")
    void minusThreeSacrificesGreatestPowerCreature() {
        Permanent onyx = addReadyOnyx(player1, 5);
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(onyx.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("-8 repeats the discard-or-life-loss process seven times")
    void minusEightRepeatsSevenTimes() {
        addReadyOnyx(player1, 8);
        harness.setLife(player2, 30);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("-8 lets an opponent discard instead of losing life")
    void minusEightAllowsDiscarding() {
        addReadyOnyx(player1, 8);
        harness.setLife(player2, 20);
        Card discarded = new GiantGrowth();
        harness.setHand(player2, List.of(discarded));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(2);
        harness.assertInGraveyard(player2, discarded.getName());
    }

    private Permanent addReadyOnyx(Player player, int loyalty) {
        Permanent permanent = new Permanent(new ProfessorOnyx());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
