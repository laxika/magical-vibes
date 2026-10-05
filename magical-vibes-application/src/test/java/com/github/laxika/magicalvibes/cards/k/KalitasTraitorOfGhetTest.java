package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FlayingTendrils;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SlaughterDrone;
import com.github.laxika.magicalvibes.cards.t.TarSnare;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.v.VampireEnvoy;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KalitasTraitorOfGhet.class, Shock.class, SlaughterDrone.class,
        TarSnare.class, TurnToFrog.class, VampireEnvoy.class, FlayingTendrils.class})
class KalitasTraitorOfGhetTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's nontoken creature and creates a Zombie")
    void exilesNontokenOpponentCreatureAndCreatesZombie() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());

        destroyCreature(player1, bears);

        harness.assertNotInGraveyard(player2, "Slaughter Drone");
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
        harness.addToBattlefield(player1, new VampireEnvoy());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kalitas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Vampire Envoy");
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    @Test
    void doesNotExileItsControllersCreature() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());

        killDrone(drone);

        harness.assertInGraveyard(player1, "Slaughter Drone");
        assertThat(gd.findExiledCard(drone.getCard().getId())).isNull();
        assertThat(countTokens(player1, "Zombie")).isZero();
    }

    @Test
    void losingAbilitiesDisablesDeathReplacement() {
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent drone = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, kalitas.getId());

        killDrone(drone);

        harness.assertInGraveyard(player2, "Slaughter Drone");
        assertThat(gd.findExiledCard(drone.getCard().getId())).isNull();
        assertThat(countTokens(player1, "Zombie")).isZero();
    }

    @Test
    void opponentChoosesBetweenKalitasAndFlayingTendrilsReplacements() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player2, new SlaughterDrone());

        harness.castFromHand(player1, new FlayingTendrils(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(countTokens(player1, "Zombie")).isZero();
    }

    @Test
    void canSacrificeARealVampireWithoutTappingKalitas() {
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new VampireEnvoy());
        kalitas.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Vampire Envoy");
        assertThat(kalitas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(kalitas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canSacrificeTheZombieCreatedByItsReplacement() {
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent drone = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());
        killDrone(drone);
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(countTokens(player1, "Zombie")).isZero();
        harness.passBothPriorities();

        assertThat(kalitas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotSacrificeItselfOrAnOpponentsVampire() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player2, new VampireEnvoy());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Vampire Envoy");
        assertThat(gd.stack).isEmpty();
    }

    private void killDrone(Permanent drone) {
        harness.setHand(player1, List.of(new TarSnare()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, drone.getId());
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
