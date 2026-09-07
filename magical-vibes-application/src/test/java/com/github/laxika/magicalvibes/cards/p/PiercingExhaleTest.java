package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PiercingExhale.class, AirElemental.class, ChandraNalaar.class, DragonWhelp.class, GrizzlyBears.class})
class PiercingExhaleTest extends BaseCardTest {

    @Test
    @DisplayName("A beheld Dragon enables surveil 2 after power damage")
    void beheldDragonEnablesSurveil() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        Card secondCard = new AirElemental();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new PiercingExhale()));
        addMana();

        castWithBehold(List.of(source.getId(), target.getId()), List.of(dragon.getId()), List.of());
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard, secondCard);
    }

    @Test
    @DisplayName("Without behold, the spell still deals power damage but does not surveil")
    void withoutBeholdOmitsSurveil() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PiercingExhale()));
        addMana();

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can deal power damage to a planeswalker")
    void dealsPowerDamageToPlaneswalker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent planeswalker = new Permanent(new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new PiercingExhale()));
        addMana();

        harness.castInstant(player1, 0, List.of(source.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects an opponent's creature as the source target")
    void rejectsOpponentCreatureAsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new PiercingExhale()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void castWithBehold(List<UUID> targetIds, List<UUID> beholdPermanentIds,
                                List<Integer> beholdHandCardIndices) {
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, targetIds, List.<UUID>of(), false,
                null, null, List.<UUID>of(), null, List.<Integer>of(), false, null,
                List.<Integer>of(), List.<UUID>of(), List.<UUID>of(), List.<String>of(), false,
                null, null, beholdPermanentIds, beholdHandCardIndices, null);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
