package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZombieScavengers.class, GrizzlyBears.class, GiantGrowth.class, Shock.class})
class ZombieScavengersTest extends BaseCardTest {

    private List<String> graveyardNames() {
        return gd.playerGraveyards.get(player1.getId()).stream().map(c -> c.getName()).toList();
    }

    @Test
    @DisplayName("Exiles the top creature card of the graveyard to gain a regeneration shield")
    void exilesTopCreatureCardForShield() {
        harness.addToBattlefield(player1, new ZombieScavengers());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Scavengers").getRegenerationShield()).isEqualTo(1);
        assertThat(graveyardNames()).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature cards above the top creature card are skipped")
    void skipsNoncreatureCardsAboveIt() {
        harness.addToBattlefield(player1, new ZombieScavengers());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GiantGrowth()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(graveyardNames()).containsExactly("Giant Growth");
        assertThat(findPermanent(player1, "Zombie Scavengers").getRegenerationShield()).isEqualTo(1);
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        harness.addToBattlefield(player1, new ZombieScavengers());
        harness.setGraveyard(player1, List.of(new GiantGrowth()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(graveyardNames()).containsExactly("Giant Growth");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Each activation exiles one creature card and creates one regeneration shield")
    void repeatedActivationsUseSeparateCreatureCards() {
        harness.addToBattlefield(player1, new ZombieScavengers());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Scavengers").getRegenerationShield()).isEqualTo(2);
        assertThat(graveyardNames()).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot use a creature card in an opponent's graveyard")
    void cannotUseOpponentsGraveyard() {
        harness.addToBattlefield(player1, new ZombieScavengers());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(c -> c.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The regeneration shield saves it from lethal damage and is spent")
    void shieldSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new ZombieScavengers());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        UUID scavengersId = harness.getPermanentId(player1, "Zombie Scavengers");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, scavengersId);

        Permanent scavengers = findPermanent(player1, "Zombie Scavengers");
        assertThat(scavengers.getRegenerationShield()).isZero();
        assertThat(scavengers.isTapped()).isTrue();
        assertThat(scavengers.getMarkedDamage()).isZero();
    }
}
