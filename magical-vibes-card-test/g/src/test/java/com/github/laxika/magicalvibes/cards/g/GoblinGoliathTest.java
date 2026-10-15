package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinGoliath.class, Shock.class, SerraAngel.class, GrizzlyBears.class})
class GoblinGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates one Goblin token for the opponent")
    void createsGoblinForOpponent() {
        harness.setHand(player1, List.of(new GoblinGoliath()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(1);
        assertThat(goblins.getFirst().getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(goblins.getFirst().getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(goblins.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(goblins.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubles damage from your sources to an opponent")
    void doublesDamageToOpponent() {
        activateDamageDoubling();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not double damage to an opponent's permanent")
    void doesNotDoubleDamageToOpponentPermanent() {
        harness.addToBattlefield(player1, new GoblinGoliath());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Does not double damage from an opponent's source")
    void doesNotDoubleOpponentsSourceDamage() {
        harness.addToBattlefield(player1, new GoblinGoliath());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles combat damage to an opponent")
    void doublesCombatDamageToOpponent() {
        harness.setLife(player2, 20);
        activateDamageDoubling();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Damage is not doubled before activating the ability")
    void doesNotDoubleDamageWithoutActivation() {
        harness.addToBattlefield(player1, new GoblinGoliath());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Resolving the activated ability pays mana and taps Goblin Goliath")
    void activationPaysManaAndTaps() {
        Permanent goliath = addCreatureReady(player1, new GoblinGoliath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(goliath.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Activated damage doubling persists after Goblin Goliath leaves")
    void doublingPersistsAfterSourceLeaves() {
        activateDamageDoubling();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Activated damage doubling ends with the turn")
    void doublingExpiresAtEndOfTurn() {
        activateDamageDoubling();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Activated doubling does not affect damage to opponent creatures")
    void activatedDoublingDoesNotAffectCreatureDamage() {
        activateDamageDoubling();
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Activated doubling does not affect damage to its controller")
    void activatedDoublingDoesNotAffectSelfDamage() {
        activateDamageDoubling();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Activated doubling does not affect damage from opponent sources")
    void activatedDoublingDoesNotAffectOpponentSources() {
        activateDamageDoubling();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    private void activateDamageDoubling() {
        addCreatureReady(player1, new GoblinGoliath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
    }
}
