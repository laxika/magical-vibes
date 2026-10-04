package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HourOfDefeat.class, Forest.class, GrizzlyBears.class, HydraTroopers.class})
class HourOfDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and surveils one")
    void destroysTargetCreatureAndSurveilsOne() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new HourOfDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HourOfDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Surveil may leave the top card in place without changing library order")
    void mayKeepTopCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HydraTroopers());
        Card topCard = new Forest();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new HourOfDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard, secondCard);
        harness.assertInGraveyard(player1, "Hour of Defeat");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An illegal target prevents surveil as well as destruction")
    void doesNotSurveilWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HydraTroopers());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new HourOfDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player2, "HYDRA Troopers");
        harness.assertInGraveyard(player1, "Hour of Defeat");
    }

    @Test
    @DisplayName("Surveil still happens when the creature is indestructible")
    void surveilsWhenDestructionIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HydraTroopers());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Card topCard = new Forest();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new HourOfDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "HYDRA Troopers");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard).doesNotContain(secondCard);
        harness.assertInGraveyard(player1, "Hour of Defeat");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not prevent destruction or require a surveil choice")
    void resolvesWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HydraTroopers());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HourOfDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "HYDRA Troopers");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Hour of Defeat");
        harness.assertLife(player1, 20);
    }
}
