package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BoneyardWurm;
import com.github.laxika.magicalvibes.cards.e.Explore;
import com.github.laxika.magicalvibes.cards.j.JagwaspSwarm;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadReckoning.class, JagwaspSwarm.class, LeatherbackBaloth.class, Explore.class})
class DeadReckoningTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the graveyard creature on top and deals damage equal to its power")
    void putsCreatureOnTopAndDealsPowerDamage() {
        Card graveyardCreature = new JagwaspSwarm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new DeadReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(graveyardCreature.getId(), target.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(graveyardCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(graveyardCreature.getId()));
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not move a card or deal damage if the graveyard target left")
    void graveyardTargetLeftBeforeResolution() {
        Card graveyardCreature = new JagwaspSwarm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new DeadReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(graveyardCreature.getId(), target.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(graveyardCreature::equals);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Still puts the creature on top if the battlefield target left")
    void creatureTargetLeftBeforeResolution() {
        Card graveyardCreature = new JagwaspSwarm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new DeadReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(graveyardCreature.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(graveyardCreature);
    }

    @Test
    @DisplayName("Requires a creature card in the controller's graveyard")
    void cannotTargetNonCreatureCard() {
        Card nonCreature = new Explore();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new DeadReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(nonCreature.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayDeclineReturningTheCreatureAndDealNoDamage() {
        Card graveyardCreature = new JagwaspSwarm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setLibrary(player1, List.of(new Explore()));
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new DeadReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(graveyardCreature.getId(), target.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(originalLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCreature);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Dead Reckoning");
    }

    @Test
    @CardUsed({BoneyardWurm.class})
    void usesPowerInTheGraveyardBeforeMovingTheCard() {
        Card graveyardCreature = new BoneyardWurm();
        Card otherCreature = new JagwaspSwarm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setGraveyard(player1, List.of(graveyardCreature, otherCreature));
        harness.setHand(player1, List.of(new DeadReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(graveyardCreature.getId(), target.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(graveyardCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature).doesNotContain(graveyardCreature);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetACreatureInOpponentsGraveyard() {
        Card graveyardCreature = new JagwaspSwarm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setGraveyard(player2, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new DeadReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(graveyardCreature.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNothingWhenBothTargetsLeave() {
        Card graveyardCreature = new JagwaspSwarm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setLibrary(player1, List.of(new Explore()));
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new DeadReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(graveyardCreature.getId(), target.getId()));
        harness.setGraveyard(player1, List.of());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(originalLibrary);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Dead Reckoning");
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Requires both targets")
    void requiresBothTargets() {
        Card graveyardCreature = new JagwaspSwarm();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new DeadReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(graveyardCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
