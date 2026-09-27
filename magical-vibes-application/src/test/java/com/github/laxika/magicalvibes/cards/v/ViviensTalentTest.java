package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViviensTalent.class, KothOfTheHammer.class, GrizzlyBears.class,
        Forest.class, Island.class, Shock.class})
class ViviensTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant only a planeswalker")
    void canEnchantOnlyPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ViviensTalent()));

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("planeswalker");
    }

    @Test
    @DisplayName("The granted plus-one ability may reveal a creature or land from the top four")
    void plusOneRevealsCreatureOrLand() {
        Permanent koth = addReadyKoth(3);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ViviensTalent());
        talent.setAttachedTo(koth.getId());

        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        Card otherLand = new Island();
        harness.setLibrary(player1, List.of(creature, land, instant, otherLand));

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, instant, otherLand);
    }

    @Test
    @DisplayName("A nontoken creature entering under your control adds a loyalty counter")
    void nontokenCreatureEntryAddsLoyalty() {
        Permanent koth = addReadyKoth(3);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ViviensTalent());
        talent.setAttachedTo(koth.getId());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        harness.enterBattlefieldAndReturn(player1, tokenCard);
        harness.passBothPriorities();
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    private Permanent addReadyKoth(int loyalty) {
        Permanent koth = new Permanent(new KothOfTheHammer());
        koth.setCounterCount(CounterType.LOYALTY, loyalty);
        koth.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(koth);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return koth;
    }
}
