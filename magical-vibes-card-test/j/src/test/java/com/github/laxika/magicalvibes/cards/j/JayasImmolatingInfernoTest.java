package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BlackbladeReforged;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({JayasImmolatingInferno.class, ArvadTheCursed.class, GrizzlyBears.class,
        GiantSpider.class, AirElemental.class, JayaBallard.class, BlackbladeReforged.class})
class JayasImmolatingInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendaryPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // non-legendary
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast when controlling a legendary creature")
    void canCastWithLegendaryCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed()); // legendary creature
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, 3, List.of(targetId));

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Jaya's Immolating Inferno");
    }

    @Test
    @DisplayName("Deals X damage to each of three creature targets")
    void dealsXDamageToThreeCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        // GrizzlyBears 2/2, GiantSpider 2/4, AirElemental 4/4
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID spiderId = harness.addToBattlefieldAndReturn(player2, new GiantSpider()).getId();
        UUID elementalId = harness.addToBattlefieldAndReturn(player2, new AirElemental()).getId();
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5); // X=3, cost {3}{R}{R}

        harness.castSorcery(player1, 0, 3, List.of(bearsId, spiderId, elementalId));
        harness.passBothPriorities();

        // GrizzlyBears took 3 damage (dies: 3 >= 2 toughness)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // GiantSpider took 3 damage (survives: 3 < 4 toughness)
        harness.assertOnBattlefield(player2, "Giant Spider");

        // AirElemental took 3 damage (survives: 3 < 4 toughness)
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Deals X damage to a single target")
    void dealsXDamageToSingleTarget() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 4); // X=2, cost {2}{R}{R}

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, 2, List.of(bearsId));
        harness.passBothPriorities();

        // GrizzlyBears took 2 damage (dies: 2 >= 2 toughness)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals X damage to player targets")
    void dealsXDamageToPlayers() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 6); // X=4, cost {4}{R}{R}

        harness.castSorcery(player1, 0, 4, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Deals X damage to mix of creatures and players")
    void dealsXDamageToMixedTargets() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5); // X=3, cost {3}{R}{R}

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, 3, List.of(bearsId, player2.getId()));
        harness.passBothPriorities();

        // GrizzlyBears took 3 damage (dies)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Player 2 took 3 damage
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Partially resolves when one creature target is removed before resolution")
    void partiallyResolvesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5); // X=3

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        harness.castSorcery(player1, 0, 3, List.of(bearsId, spiderId, player2.getId()));

        // Remove bears before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Bears was removed before resolution.
        // GiantSpider took 3 damage (survives: 3 < 4 toughness)
        harness.assertOnBattlefield(player2, "Giant Spider");
        // Player 2 took 3 damage
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 4); // X=2

        harness.castSorcery(player1, 0, 2, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Jaya's Immolating Inferno");
    }

    @Test
    @DisplayName("Can cast with zero targets and positive X")
    void canCastWithZeroTargets() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, List.of());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Arvad the Cursed");
        harness.assertInGraveyard(player1, "Jaya's Immolating Inferno");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Zero X deals no damage to chosen targets")
    void zeroXDealsNoDamage() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, List.of(bearsId, player2.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Jaya's Immolating Inferno");
    }

    @Test
    @DisplayName("Legendary planeswalker permits casting and can be targeted")
    void canCastWithAndDamageLegendaryPlaneswalker() {
        Permanent jaya = harness.addToBattlefieldAndReturn(player1, new JayaBallard());
        jaya.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, List.of(jaya.getId(), player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Jaya Ballard");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An opponent's legendary creature does not permit casting")
    void opponentsLegendaryCreatureDoesNotPermitCasting() {
        harness.addToBattlefield(player2, new ArvadTheCursed());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Jaya's Immolating Inferno");
    }

    @Test
    @DisplayName("A legendary artifact alone does not permit casting")
    void legendaryArtifactDoesNotPermitCasting() {
        harness.addToBattlefield(player1, new BlackbladeReforged());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Jaya's Immolating Inferno");
    }

    @Test
    @DisplayName("Losing the legendary creature after casting does not stop resolution")
    void resolvesAfterLegendaryCreatureLeaves() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, List.of(player2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(arvad);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Jaya's Immolating Inferno");
    }

    @Test
    @DisplayName("Cannot choose the same target twice")
    void cannotChooseDuplicateTargets() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Jaya's Immolating Inferno");
    }

    @Test
    @DisplayName("Cannot choose more than three targets")
    void cannotChooseFourTargets() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        Permanent jaya = harness.addToBattlefieldAndReturn(player2, new JayaBallard());
        jaya.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3,
                List.of(arvad.getId(), jaya.getId(), player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Jaya's Immolating Inferno");
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new BlackbladeReforged()).getId();
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Jaya's Immolating Inferno");
    }

    @Test
    @DisplayName("Does not resolve when all chosen targets have left the battlefield")
    void allTargetsIllegal() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, List.of(bears.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Jaya's Immolating Inferno");
        assertThat(gd.stack).isEmpty();
    }
}
