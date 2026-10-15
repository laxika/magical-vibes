package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnarlingGorehound.class, GrizzlyBears.class, HillGiant.class, GloriousAnthem.class})
class SnarlingGorehoundTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 when another creature with power 2 or less enters under its controller's control")
    void surveilsWhenSmallAllyEnters() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        Card topCard = new HillGiant();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power greater than 2")
    void doesNotTriggerForLargeCreature() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for itself entering the battlefield")
    void doesNotTriggerForItself() {
        harness.castFromHand(player1, new SnarlingGorehound(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May leave the surveilled card on top of the library")
    void mayKeepTopCard() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        Card topCard = new SnarlingGorehound();
        harness.setLibrary(player1, List.of(topCard));

        harness.castFromHand(player1, new SnarlingGorehound(), "{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Surveil resolves with an empty library")
    void resolvesWithEmptyLibrary() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new SnarlingGorehound(), "{B}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a continuous boost makes entering power greater than 2")
    void checksEffectiveEnteringPower() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setLibrary(player1, List.of(new SnarlingGorehound()));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A power increase after entry does not prevent surveil")
    void powerIncreaseAfterEntryDoesNotPreventSurveil() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        Card topCard = new SnarlingGorehound();
        harness.setLibrary(player1, List.of(topCard));

        harness.castFromHand(player1, new SnarlingGorehound(), "{B}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        entering.setPowerModifier(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }
}


