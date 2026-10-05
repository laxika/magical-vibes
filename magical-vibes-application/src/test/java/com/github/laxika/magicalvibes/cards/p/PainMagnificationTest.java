package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CacklingFlames;
import com.github.laxika.magicalvibes.cards.d.Demonfire;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainMagnification.class, CacklingFlames.class, Demonfire.class, MistralCharger.class})
class PainMagnificationTest extends BaseCardTest {

    @Test
    void makesTheDamagedOpponentDiscardAfterThreeDamage() {
        harness.addToBattlefield(player1, new PainMagnification());
        harness.setHand(player1, List.of(new CacklingFlames(), new CacklingFlames()));
        harness.setHand(player2, List.of(new MistralCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerBelowThreeDamage() {
        harness.addToBattlefield(player1, new PainMagnification());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.setHand(player2, List.of(new MistralCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void doesNotCombineDamageFromSeparateCombatSources() {
        harness.addToBattlefield(player1, new PainMagnification());
        Permanent attackerOne = addCreatureReady(player1, new MistralCharger());
        Permanent attackerTwo = addCreatureReady(player1, new MistralCharger());
        harness.setHand(player2, List.of(new MistralCharger()));
        attackerOne.setAttacking(true);
        attackerTwo.setAttacking(true);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenItsControllerIsDealtDamage() {
        harness.addToBattlefield(player1, new PainMagnification());
        harness.setHand(player1, List.of(new MistralCharger()));
        harness.setHand(player2, List.of(new CacklingFlames(), new CacklingFlames()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void discardsOnlyOneCardForFiveDamageFromOneSource() {
        harness.addToBattlefield(player1, new PainMagnification());
        harness.setHand(player1, List.of(new CacklingFlames()));
        harness.setHand(player2, List.of(new MistralCharger(), new MistralCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void triggersWhenTheOpponentDamagesThemself() {
        harness.addToBattlefield(player1, new PainMagnification());
        harness.setHand(player2, List.of(new CacklingFlames(), new MistralCharger()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutAChoiceWhenTheDamagedOpponentHasNoCards() {
        harness.addToBattlefield(player1, new PainMagnification());
        harness.setHand(player1, List.of(new CacklingFlames()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
