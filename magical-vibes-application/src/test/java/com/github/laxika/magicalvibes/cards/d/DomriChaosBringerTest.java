package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomriChaosBringerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 adds chosen mana that gives a creature spell riot")
    void plusOneGrantsRiotToCreatureSpellPaidWithMana() {
        Permanent domri = addReadyDomri(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getRiotGrantingManaTotal()).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 puts up to two creature cards from the top four into hand")
    void minusThreePutsUpToTwoCreaturesIntoHand() {
        addReadyDomri(player1);
        Card bears = new GrizzlyBears();
        Card bolt = new LightningBolt();
        Card angel = new SerraAngel();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(bears, bolt, angel, plains));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), angel.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), angel.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears, angel);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bolt, plains);
    }

    @Test
    @DisplayName("-8 creates an emblem that makes a Beast at each end step")
    void minusEightCreatesEndStepBeastEmblem() {
        Permanent domri = addReadyDomri(player1);
        domri.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyDomri(Player player) {
        Permanent perm = new Permanent(new DomriChaosBringer());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
