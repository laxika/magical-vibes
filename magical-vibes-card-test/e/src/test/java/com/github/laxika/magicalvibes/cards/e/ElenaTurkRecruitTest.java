package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QueenMarchesa;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.y.YunaGrandSummoner;
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

@CardUsed({ElenaTurkRecruit.class, Spellbook.class, QueenMarchesa.class, GrizzlyBears.class,
        YunaGrandSummoner.class})
class ElenaTurkRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a non-Assassin historic card from the graveyard")
    void returnsNonAssassinHistoricCard() {
        Card artifact = new Spellbook();
        Card legendaryAssassin = new QueenMarchesa();
        Card nonHistoric = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, legendaryAssassin, nonHistoric));
        harness.castFromHand(player1, new ElenaTurkRecruit(), "{2}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Queen Marchesa");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a historic spell puts a +1/+1 counter on Elena")
    void historicSpellAddsCounter() {
        Permanent elena = harness.addToBattlefieldAndReturn(player1, new ElenaTurkRecruit());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(elena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a nonhistoric spell does not put a counter on Elena")
    void nonHistoricSpellDoesNotAddCounter() {
        Permanent elena = harness.addToBattlefieldAndReturn(player1, new ElenaTurkRecruit());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(elena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void returnsLegendaryNonArtifactFromOwnGraveyardOnly() {
        Card legendary = new YunaGrandSummoner();
        Card assassin = new ElenaTurkRecruit();
        Card opponentsCard = new YunaGrandSummoner();
        harness.setGraveyard(player1, List.of(legendary, assassin));
        harness.setGraveyard(player2, List.of(opponentsCard));

        harness.castFromHand(player1, new ElenaTurkRecruit(), "{2}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legendary.getId());
        harness.handleMultipleCardsChosen(player1, List.of(legendary.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Yuna, Grand Summoner");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(assassin);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    void entersWithoutEligibleGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new ElenaTurkRecruit()));
        harness.castFromHand(player1, new ElenaTurkRecruit(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elena, Turk Recruit");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void legendarySpellAddsCounterBeforeSpellResolves() {
        Permanent elena = harness.addToBattlefieldAndReturn(player1, new ElenaTurkRecruit());
        harness.castFromHand(player1, new YunaGrandSummoner(), "{1}{G}{W}{U}");

        assertThat(gd.stack).hasSize(2);
        assertThat(elena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(elena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Yuna, Grand Summoner");
    }

    @Test
    void opponentsHistoricSpellDoesNotAddCounter() {
        Permanent elena = harness.addToBattlefieldAndReturn(player1, new ElenaTurkRecruit());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ElenaTurkRecruit(), "{2}{W}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(elena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void targetLeavingGraveyardDoesNotReturnAnotherCard() {
        Card target = new YunaGrandSummoner();
        Card other = new YunaGrandSummoner();
        harness.setGraveyard(player1, List.of(target, other));
        harness.castFromHand(player1, new ElenaTurkRecruit(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
