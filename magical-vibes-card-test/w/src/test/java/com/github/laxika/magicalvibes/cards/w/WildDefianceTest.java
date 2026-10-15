package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NaturalAffinity;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
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

@CardUsed({WildDefiance.class, GrizzlyBears.class, GiantGrowth.class, Shock.class,
        ElaborateFirecannon.class, PillarOfFlame.class, NaturalAffinity.class, Forest.class})
class WildDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Own instant targeting your creature gives it +3/+3")
    void triggersOnOwnInstant() {
        harness.addToBattlefield(player1, new WildDefiance());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bearsId);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Wild Defiance");

        resolveAllTriggers();

        Permanent bears = bears(bearsId);
        assertThat(bears.getEffectivePower()).isEqualTo(8);
        assertThat(bears.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Opponent's burn spell triggers the boost, saving the creature")
    void triggersOnOpponentInstant() {
        harness.addToBattlefield(player1, new WildDefiance());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);
        resolveAllTriggers();

        Permanent bears = bears(bearsId);
        assertThat(bears).isNotNull();
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a creature an opponent controls")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new WildDefiance());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Does not trigger for an activated ability targeting your creature")
    void doesNotTriggerForActivatedAbility() {
        harness.addToBattlefield(player1, new WildDefiance());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        addCreatureReady(player2, new ElaborateFirecannon());

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType().name()).contains("ABILITY");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new WildDefiance());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bearsId);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = bears(bearsId);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sorcery targeting your creature triggers before damage")
    void triggersOnSorcery() {
        harness.addToBattlefield(player1, new WildDefiance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
        assertThat(creature.getMarkedDamage()).isZero();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Wild Defiance independently boosts the targeted creature")
    void multipleEnchantmentsTrigger() {
        harness.addToBattlefield(player1, new WildDefiance());
        harness.addToBattlefield(player1, new WildDefiance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();
        assertThat(creature.getEffectivePower()).isEqualTo(8);
        assertThat(creature.getEffectiveToughness()).isEqualTo(8);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }
    @Test
    @DisplayName("An animated land is a creature for Wild Defiance")
    void triggersForAnimatedLand() {
        harness.addToBattlefield(player1, new WildDefiance());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new NaturalAffinity(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, land)).isTrue();

        harness.castInstant(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(land.getMarkedDamage()).isEqualTo(2);
    }
    private Permanent bears(UUID permanentId) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getId().equals(permanentId))
                .findFirst()
                .orElse(null);
    }
}
