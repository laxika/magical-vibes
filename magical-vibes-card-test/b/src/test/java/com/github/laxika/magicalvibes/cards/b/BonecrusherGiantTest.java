package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BonecrusherGiant.class, Stomp.class, Shock.class, GrizzlyBears.class, LoxodonWarhammer.class})
class BonecrusherGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the controller of a spell that targets it")
    void damagesSpellController() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new BonecrusherGiant());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Stomp deals 2 damage to a creature and damage cannot be prevented this turn")
    void stompDealsUnpreventableDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setDamagePreventionShield(5);

        BonecrusherGiant card = new BonecrusherGiant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("The creature face can be cast from exile after Stomp")
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        BonecrusherGiant card = new BonecrusherGiant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonecrusher Giant");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    @DisplayName("Targeting your own Giant deals damage to you before Stomp resolves")
    void ownSpellTriggersBeforeResolving() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new BonecrusherGiant());
        harness.setHand(player1, List.of(new BonecrusherGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(gd.damageCantBePreventedThisTurn).isFalse();

        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The Giant's triggered damage uses lifelink granted by equipment")
    void triggeredDamageUsesGrantedLifelink() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new BonecrusherGiant());
        Permanent warhammer = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        warhammer.setAttachedTo(giant.getId());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BonecrusherGiant()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAdventure(player2, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An illegal Stomp target stops all effects and the adventure exile")
    void illegalTargetStopsAllStompEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BonecrusherGiant card = new BonecrusherGiant();
        harness.setHand(player1, List.of(card, new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bonecrusher Giant");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }

    @Test
    @DisplayName("Stomp stops prevention for subsequent damage from either player")
    void stompStopsSubsequentDamagePrevention() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setDamagePreventionShield(5);
        harness.setHand(player1, List.of(new BonecrusherGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
