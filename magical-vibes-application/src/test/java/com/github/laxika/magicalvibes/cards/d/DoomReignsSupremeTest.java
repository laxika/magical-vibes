package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BaronHelmutZemo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomReignsSupreme.class, BaronHelmutZemo.class, Forest.class, GrizzlyBears.class, Shock.class})
class DoomReignsSupremeTest extends BaseCardTest {

    @Test
    @DisplayName("A Villain entering under your control drains opponents and adds a plan counter")
    void villainEnteringDrainsAndAddsPlanCounter() {
        Permanent doom = addDoom();

        harness.enterBattlefieldAndReturn(player1, new BaronHelmutZemo());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(doom.getCounterCount(CounterType.PLAN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The fifth plan counter sacrifices Doom and offers at most two spells from a targeted opponent's library")
    void fifthPlanCounterSacrificesAndOffersTwoSpells() {
        Permanent doom = addDoom();
        doom.setCounterCount(CounterType.PLAN, 4);
        Card firstSpell = new GrizzlyBears();
        Card secondSpell = new Shock();
        Card thirdSpell = new GrizzlyBears();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player2, List.of(firstSpell, secondSpell, thirdSpell, firstLand, secondLand));

        harness.enterBattlefieldAndReturn(player1, new BaronHelmutZemo());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doom);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(doom.getCard());
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(doom.getId()))
                .extracting(Card::getId)
                .containsExactly(firstSpell.getId(), secondSpell.getId(), thirdSpell.getId(),
                        firstLand.getId(), secondLand.getId());
        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).containsExactlyInAnyOrder(
                firstSpell.getId(), secondSpell.getId(), thirdSpell.getId());
        assertThat(castChoice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(firstSpell.getId(), secondSpell.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(firstSpell.getId())).isNull();
        assertThat(gd.findExiledCard(secondSpell.getId())).isNull();
        assertThat(gd.findExiledCard(thirdSpell.getId())).isNotNull();
        assertThat(gd.findExiledCard(firstLand.getId())).isNotNull();
        assertThat(gd.findExiledCard(secondLand.getId())).isNotNull();
    }

    @Test
    @DisplayName("A non-Villain entering under your control does not advance the plan")
    void nonVillainDoesNotAdvancePlan() {
        Permanent doom = addDoom();

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(doom.getCounterCount(CounterType.PLAN)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent addDoom() {
        return harness.addToBattlefieldAndReturn(player1, new DoomReignsSupreme());
    }
}
