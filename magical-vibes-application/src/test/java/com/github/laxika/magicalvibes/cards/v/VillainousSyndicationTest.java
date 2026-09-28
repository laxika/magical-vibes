package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MODOK;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VillainousSyndication.class, MODOK.class, Forest.class, GrizzlyBears.class})
class VillainousSyndicationTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Villain mills a card and puts a plan counter on Villainous Syndication")
    void tapsVillainMillsAndAddsPlanCounter() {
        Permanent syndication = harness.addToBattlefieldAndReturn(player1, new VillainousSyndication());
        Permanent villain = addCreatureReady(player1, new MODOK());
        Forest milledCard = new Forest();
        harness.setLibrary(player1, List.of(milledCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(villain.isTapped()).isTrue();
        assertThat(syndication.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
    }

    @Test
    @DisplayName("The fourth plan counter sacrifices Villainous Syndication and returns a target creature")
    void fourthPlanCounterSacrificesAndReturnsCreature() {
        Permanent syndication = harness.addToBattlefieldAndReturn(player1, new VillainousSyndication());
        syndication.setCounterCount(CounterType.PLAN, 3);
        Permanent villain = addCreatureReady(player1, new MODOK());
        Forest milledCard = new Forest();
        GrizzlyBears returnedCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milledCard));
        harness.setGraveyard(player1, List.of(returnedCreature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(villain.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(syndication);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(syndication.getCard());
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(returnedCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(returnedCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnedCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(returnedCreature.getId()));
    }

    @Test
    @DisplayName("The ability is restricted to sorcery speed")
    void abilityIsSorcerySpeedOnly() {
        harness.addToBattlefield(player1, new VillainousSyndication());
        addCreatureReady(player1, new MODOK());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }
}
