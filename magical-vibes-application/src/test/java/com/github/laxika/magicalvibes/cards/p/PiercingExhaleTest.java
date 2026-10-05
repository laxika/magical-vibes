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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        harness.setLibrary(player1, List.of(topCard, secondCard));
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
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
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

    @Test
    @DisplayName("Revealing a Dragon from hand enables surveil without discarding it")
    void revealedDragonRemainsInHand() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card dragon = new DragonWhelp();
        Card topCard = new GrizzlyBears();
        Card secondCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new PiercingExhale(), dragon));
        addMana();

        castWithBehold(List.of(source.getId(), target.getId()), List.of(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dragon);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard).doesNotContain(secondCard, dragon);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The same creature can be both targets")
    void creatureCanDamageItself() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PiercingExhale()));
        addMana();

        harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage uses the creature's power at resolution and is not a fight")
    void usesCurrentPowerWithoutReturnDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new PiercingExhale()));
        addMana();

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Dragon Whelp");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Surveil still happens when one target leaves, but no damage is dealt")
    void surveilsWithOneRemainingTarget(boolean removeSource) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card topCard = new GrizzlyBears();
        Card secondCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new PiercingExhale()));
        addMana();

        castWithBehold(List.of(source.getId(), target.getId()), List.of(source.getId()), List.of());
        if (removeSource) {
            gd.playerBattlefields.get(player1.getId()).remove(source);
            gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        } else {
            gd.playerBattlefields.get(player2.getId()).remove(target);
            gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        }
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
    }

    @Test
    @DisplayName("No surveil happens when both targets become illegal")
    void doesNotResolveWithNoLegalTargets() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card topCard = new GrizzlyBears();
        Card secondCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new PiercingExhale()));
        addMana();

        castWithBehold(List.of(source.getId(), target.getId()), List.of(source.getId()), List.of());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        harness.assertInGraveyard(player1, "Piercing Exhale");
    }

    private void castWithBehold(List<UUID> targetIds, List<UUID> beholdPermanentIds,
                                List<Integer> beholdHandCardIndices) {
        harness.castSorceryWithBehold(player1, 0, null, targetIds,
                beholdPermanentIds, beholdHandCardIndices);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
