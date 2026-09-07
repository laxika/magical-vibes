package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BelligerentRegisaur;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelligerentRegisaur.class, Forest.class, GrizzlyBears.class,
        InvasionOfIxalan.class, Island.class, Plains.class, Shock.class})
class InvasionOfIxalanTest extends BaseCardTest {

    @Test
    @DisplayName("The Siege may reveal a permanent from the top five and bottoms the rest randomly")
    void looksAtTopFiveForPermanent() {
        Card creature = new GrizzlyBears();
        Card instant = new Shock();
        Card forest = new Forest();
        Card plains = new Plains();
        Card island = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(creature, instant, forest, plains, island));

        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).contains(creature.getId(), forest.getId(), plains.getId(), island.getId())
                .doesNotContain(instant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(instant, forest, plains, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Defeating the Siege casts Belligerent Regisaur transformed")
    void defeatCastsBackFace() {
        gd.playerDecks.get(player1.getId()).clear();
        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battle = findPermanent(player1, "Invasion of Ixalan");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent regisaur = findPermanent(player1, "Belligerent Regisaur");
        assertThat(regisaur.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Belligerent Regisaur gains indestructible when you cast a spell")
    void spellCastGrantsIndestructible() {
        Permanent regisaur = harness.addToBattlefieldAndReturn(player1, new BelligerentRegisaur());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(regisaur.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    private void castInvasion() {
        harness.setHand(player1, List.of(new InvasionOfIxalan()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCard(gd, player1, 0, 0, null, null);
    }
}
