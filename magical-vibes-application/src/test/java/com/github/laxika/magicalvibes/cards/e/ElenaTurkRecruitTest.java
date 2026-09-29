package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QueenMarchesa;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElenaTurkRecruit.class, Spellbook.class, QueenMarchesa.class, GrizzlyBears.class})
class ElenaTurkRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a non-Assassin historic card from the graveyard")
    void returnsNonAssassinHistoricCard() {
        Card artifact = new Spellbook();
        Card legendaryAssassin = new QueenMarchesa();
        Card nonHistoric = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, legendaryAssassin, nonHistoric));
        harness.setHand(player1, List.of(new ElenaTurkRecruit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(elena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }
}
