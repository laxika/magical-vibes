package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LieInWait.class, AirElemental.class, Forest.class, GoblinPiker.class, GrizzlyBears.class})
class LieInWaitTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Test
    @DisplayName("Returns a creature card to hand and deals its power in damage to target creature")
    void returnsCreatureAndDamagesCreature() {
        Card graveyardCreature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new LieInWait()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Deals enough damage to destroy the target creature")
    void destroysTargetCreature() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new LieInWait()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Goblin Piker");
        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(targetId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        harness.assertInGraveyard(player2, "Goblin Piker");
    }

    @Test
    @DisplayName("Does not return or deal damage when the graveyard target left before resolution")
    void graveyardTargetRemovedNoDamage() {
        Card graveyardCreature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new LieInWait()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Still returns the card when the creature target left before resolution")
    void creatureTargetRemovedStillReturnsCard() {
        Card graveyardCreature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new LieInWait()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Card graveyardLand = new Forest();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(graveyardLand));
        harness.setHand(player1, List.of(new LieInWait()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, graveyardLand.getId(), List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
