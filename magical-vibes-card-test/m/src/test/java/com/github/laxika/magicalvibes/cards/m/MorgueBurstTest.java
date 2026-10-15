package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BoneyardWurm;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.r.RakdosCluestone;
import com.github.laxika.magicalvibes.cards.r.RiotPiker;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorgueBurst.class, KraulWarrior.class, RiotPiker.class, RakdosCluestone.class,
        BoneyardWurm.class, InvasionOfZendikar.class})
class MorgueBurstTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 5); // 1 for {R}, 4 for generic
    }

    @Test
    @DisplayName("Returns creature card to hand and deals its power in damage to target player")
    void returnsCreatureAndDamagesPlayer() {
        Card graveyardCreature = new KraulWarrior(); // 2/2
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new MorgueBurst()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Deals power damage to a target creature")
    void damagesTargetCreature() {
        Card graveyardCreature = new KraulWarrior(); // 2/2
        harness.addToBattlefield(player2, new RiotPiker()); // 2/1, dies to 2 damage
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new MorgueBurst()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID victimId = harness.getPermanentId(player2, "Riot Piker");

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(victimId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Riot Piker");
        harness.assertInGraveyard(player2, "Riot Piker");
    }

    @Test
    @DisplayName("Graveyard target removed before resolution — no return and no damage")
    void graveyardTargetRemovedNoDamage() {
        Card graveyardCreature = new KraulWarrior();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new MorgueBurst()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(player2.getId()));

        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void cannotTargetNoncreatureCard() {
        Card graveyardArtifact = new RakdosCluestone();
        harness.setGraveyard(player1, List.of(graveyardArtifact));
        harness.setHand(player1, List.of(new MorgueBurst()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, graveyardArtifact.getId(), List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in another player's graveyard")
    void cannotTargetOpponentGraveyardCard() {
        Card ownCard = new KraulWarrior();
        Card opponentCard = new RiotPiker();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new MorgueBurst()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCard.getId(), List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses dynamic power as the creature last existed in the graveyard")
    void usesLastGraveyardPower() {
        Card wurm = new BoneyardWurm();
        harness.setGraveyard(player1, List.of(wurm, new KraulWarrior(), new RiotPiker()));
        harness.setHand(player1, List.of(new MorgueBurst()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, wurm.getId(), List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Boneyard Wurm");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A battle is a legal damage target")
    void canTargetBattle() {
        Card creature = new KraulWarrior();
        var battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MorgueBurst()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId(), List.of(battle.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kraul Warrior");
        harness.assertOnBattlefield(player2, "Invasion of Zendikar");
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns the creature even when the damage target has left the battlefield")
    void returnsCreatureWhenDamageTargetDisappears() {
        Card creature = new KraulWarrior();
        var victim = harness.addToBattlefieldAndReturn(player2, new RiotPiker());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MorgueBurst()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId(), List.of(victim.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        harness.setGraveyard(player2, List.of(victim.getCard()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kraul Warrior");
        harness.assertNotInGraveyard(player1, "Kraul Warrior");
        harness.assertLife(player2, 20);
    }
}
