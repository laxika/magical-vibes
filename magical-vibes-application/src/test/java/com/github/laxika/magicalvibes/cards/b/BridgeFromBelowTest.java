package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.s.SproutSwarm;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BridgeFromBelow.class, BladeOfTheSixthPride.class, Ghostfire.class, SproutSwarm.class})
class BridgeFromBelowTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature entering your graveyard creates a Zombie")
    void ownNontokenCreatureDeathCreatesZombie() {
        harness.setGraveyard(player1, List.of(new BridgeFromBelow()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSixthPride());

        destroyCreature(player1, creature.getId());

        List<Permanent> zombies = findPermanents(player1, "Zombie");
        assertThat(zombies).hasSize(1);
        assertThat(zombies.getFirst().getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(zombies.getFirst().getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombies.getFirst().getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombies.getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(zombies.getFirst().getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature entering an opponent's graveyard exiles Bridge from Below")
    void opponentCreatureDeathExilesBridge() {
        BridgeFromBelow bridge = new BridgeFromBelow();
        harness.setGraveyard(player1, List.of(bridge));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());

        destroyCreature(player2, creature.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bridge);
        harness.assertNotInGraveyard(player1, "Bridge from Below");
    }

    @Test
    @DisplayName("Bridge from Below ignores token creatures for its Zombie trigger")
    void tokenCreatureDeathDoesNotCreateZombie() {
        harness.setGraveyard(player1, List.of(new BridgeFromBelow()));
        Permanent token = createSaproling(player1);

        destroyCreature(player1, token.getId());

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.assertInGraveyard(player1, "Bridge from Below");
    }

    @Test
    @DisplayName("A token creature entering an opponent's graveyard exiles Bridge from Below")
    void opponentTokenCreatureDeathExilesBridge() {
        BridgeFromBelow bridge = new BridgeFromBelow();
        harness.setGraveyard(player1, List.of(bridge));
        Permanent token = createSaproling(player2);

        destroyCreature(player2, token.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bridge);
        harness.assertNotInGraveyard(player1, "Bridge from Below");
    }

    @Test
    @DisplayName("Bridge from Below has no effect while on the battlefield")
    void battlefieldBridgeDoesNothing() {
        harness.addToBattlefield(player1, new BridgeFromBelow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSixthPride());

        destroyCreature(player1, creature.getId());

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.assertOnBattlefield(player1, "Bridge from Below");
    }

    @Test
    @DisplayName("Simultaneous own and opponent creature deaths create a Zombie before Bridge is exiled")
    void simultaneousCreatureDeathsCreateZombieBeforeBridgeExiles() {
        BridgeFromBelow bridge = new BridgeFromBelow();
        harness.setGraveyard(player1, List.of(bridge));
        addCreatureReady(player1, new BladeOfTheSixthPride());
        addCreatureReady(player2, new BladeOfTheSixthPride());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        // Both triggers belong to player1, who can put the Zombie trigger on top.
        assertThat(gd.stack).hasSize(2);
        gd.stack.sort(java.util.Comparator.comparingInt(entry ->
                entry.getEffectsToResolve().getFirst() instanceof ConditionalEffect conditional
                        && conditional.wrapped() instanceof ExileSourceCardFromGraveyardEffect ? 0 : 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bridge);
    }

    private Permanent createSaproling(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new SproutSwarm()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player, 0);
        return findPermanent(player, "Saproling");
    }

    private void destroyCreature(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Ghostfire()));
        harness.addMana(caster, ManaColor.RED, 3);
        harness.castAndResolveInstant(caster, 0, targetId);
        resolveAllTriggers();
    }
}
