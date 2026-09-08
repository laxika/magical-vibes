package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalitasTraitorOfGhet.class, Shock.class})
class KalitasTraitorOfGhetTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's nontoken creature and creates a Zombie")
    void exilesNontokenOpponentCreatureAndCreatesZombie() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent bears = addCreature(player2, "Grizzly Bears", List.of(CardSubtype.BEAR), false);

        destroyCreature(player1, bears);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not replace an opponent's token creature")
    void doesNotReplaceTokenCreature() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent token = addCreature(player2, "Bear Token", List.of(CardSubtype.BEAR), true);

        destroyCreature(player1, token);

        assertThat(gd.findExiledCard(token.getCard().getId())).isNull();
        assertThat(countTokens(player1, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Sacrificing another Vampire or Zombie puts two +1/+1 counters on Kalitas")
    void sacrificesAnotherVampireOrZombieForCounters() {
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        addCreature(player1, "Vampire", List.of(CardSubtype.VAMPIRE), false);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kalitas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Vampire");
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player, String name, List<CardSubtype> subtypes, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(token ? "" : "{2}");
        card.setToken(token);
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(subtypes);
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private long countTokens(Player player, String subtypeName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().stream()
                        .anyMatch(subtype -> subtype.name().equalsIgnoreCase(subtypeName)))
                .count();
    }
}
