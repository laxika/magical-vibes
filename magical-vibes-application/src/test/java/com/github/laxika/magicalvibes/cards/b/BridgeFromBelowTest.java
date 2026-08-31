package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BridgeFromBelow.class, GrizzlyBears.class, Shock.class})
class BridgeFromBelowTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature entering your graveyard creates a Zombie")
    void ownNontokenCreatureDeathCreatesZombie() {
        harness.setGraveyard(player1, List.of(new BridgeFromBelow()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyCreature(player1, player1, 0);

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
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyCreature(player2, player2, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bridge);
        harness.assertNotInGraveyard(player1, "Bridge from Below");
    }

    @Test
    @DisplayName("Bridge from Below ignores token creatures for its Zombie trigger")
    void tokenCreatureDeathDoesNotCreateZombie() {
        harness.setGraveyard(player1, List.of(new BridgeFromBelow()));

        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        harness.addToBattlefield(player1, tokenCard);

        destroyCreature(player1, player1, 0);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Bridge from Below has no effect while on the battlefield")
    void battlefieldBridgeDoesNothing() {
        harness.addToBattlefield(player1, new BridgeFromBelow());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyCreature(player1, player1, 1);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.assertOnBattlefield(player1, "Bridge from Below");
    }

    private void destroyCreature(com.github.laxika.magicalvibes.model.Player caster,
                                 com.github.laxika.magicalvibes.model.Player targetController,
                                 int targetIndex) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        Permanent target = gd.playerBattlefields.get(targetController.getId()).get(targetIndex);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
