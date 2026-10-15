package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FieryMantle;
import com.github.laxika.magicalvibes.cards.g.GoblinPatrol;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.SteamBlast;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbsoluteLaw.class, CoralMerfolk.class, GoblinRaider.class, Disenchant.class, HeatRay.class,
        SteamBlast.class, Opalescence.class, GoblinPatrol.class, FieryMantle.class})
class AbsoluteLawTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have protection from red")
    void grantsProtectionFromRedToAllCreatures() {
        harness.addToBattlefield(player1, new AbsoluteLaw());
        Permanent ownMerfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinRaider());

        assertThat(gqs.hasProtectionFrom(gd, ownMerfolk, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opponentGoblin, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownMerfolk, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Protection from red ends when Absolute Law leaves the battlefield")
    void protectionEndsWhenAbsoluteLawLeaves() {
        Permanent law = harness.addToBattlefieldAndReturn(player1, new AbsoluteLaw());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        assertThat(gqs.hasProtectionFrom(gd, merfolk, CardColor.RED)).isTrue();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, law.getId());

        harness.assertInGraveyard(player1, "Absolute Law");
        assertThat(gqs.hasProtectionFrom(gd, merfolk, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection from red prevents red spells from targeting creatures")
    void preventsRedSpellFromTargetingCreature() {
        harness.addToBattlefield(player1, new AbsoluteLaw());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(merfolk.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Protection from red prevents red damage to creatures but not players")
    void preventsRedDamageToCreaturesButNotPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AbsoluteLaw());
        Permanent ownMerfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinRaider());
        harness.setHand(player1, List.of(new SteamBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(ownMerfolk.getMarkedDamage()).isZero();
        assertThat(opponentGoblin.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @CardUsed(Opalescence.class)
    @Test
    @DisplayName("An animated Absolute Law gives itself protection from red")
    void animatedAbsoluteLawProtectsItself() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent law = harness.addToBattlefieldAndReturn(player1, new AbsoluteLaw());

        assertThat(gqs.isCreature(gd, law)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, law, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Red creatures cannot block creatures protected by Absolute Law")
    void preventsRedBlockers() {
        harness.addToBattlefield(player1, new AbsoluteLaw());
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GoblinPatrol());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("An attached red Aura falls off when Absolute Law resolves")
    void removesAttachedRedAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.setHand(player1, List.of(new FieryMantle(), new AbsoluteLaw()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fiery Mantle");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fiery Mantle");
        harness.assertInGraveyard(player1, "Fiery Mantle");
        harness.assertOnBattlefield(player1, "Coral Merfolk");
        resolveAllTriggers();
        harness.assertInHand(player1, "Fiery Mantle");
    }

    @Test
    @DisplayName("A red spell loses its legal target when Absolute Law enters before resolution")
    void redSpellTargetBecomesIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of(creature.getId()));
        harness.addToBattlefield(player2, new AbsoluteLaw());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Heat Ray");
        assertThat(gd.stack).isEmpty();
    }
}
