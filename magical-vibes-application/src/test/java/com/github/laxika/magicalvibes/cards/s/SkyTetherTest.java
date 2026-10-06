package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.ArrestersZeal;
import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyTether.class, AirElemental.class, ArrestersZeal.class, ConcordiaPegasus.class, GrizzlyBears.class, Mountain.class})
class SkyTetherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sky Tether attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkyTether()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has defender and loses flying")
    void enchantedCreatureHasDefenderAndLosesFlying() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent tether = harness.addToBattlefieldAndReturn(player1, new SkyTether());
        tether.setAttachedTo(flyer.getId());

        assertThat(gqs.hasKeyword(gd, flyer, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, flyer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sky Tether does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tether = harness.addToBattlefieldAndReturn(player1, new SkyTether());
        tether.setAttachedTo(flyer.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creature regains flying and loses defender when Sky Tether leaves")
    void creatureRestoresKeywordsWhenAuraLeaves() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent tether = harness.addToBattlefieldAndReturn(player1, new SkyTether());
        tether.setAttachedTo(flyer.getId());

        gd.playerBattlefields.get(player1.getId()).remove(tether);

        assertThat(gqs.hasKeyword(gd, flyer, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, flyer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SkyTether()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Sky Tether can enchant an opponent's creature and prevents it from attacking")
    void enchantsOpponentCreature() {
        Permanent flyer = addCreatureReady(player2, new ConcordiaPegasus());
        assertThat(als.canAttack(gd, flyer, player2.getId())).isTrue();
        harness.setHand(player1, List.of(new SkyTether()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, flyer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof SkyTether && flyer.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, flyer, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, flyer, Keyword.FLYING)).isFalse();
        assertThat(als.canAttack(gd, flyer, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Sky Tether can enchant a creature without flying")
    void enchantsNonflyingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkyTether()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        harness.assertOnBattlefield(player1, "Sky Tether");
    }

    @Test
    @DisplayName("Sky Tether goes to the graveyard when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new ConcordiaPegasus());
        harness.setHand(player1, List.of(new SkyTether()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, flyer.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, flyer));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Sky Tether");
        harness.assertInGraveyard(player1, "Sky Tether");
        harness.assertInHand(player2, "Concordia Pegasus");
    }

    @Test
    @DisplayName("A later flying grant applies while Sky Tether still grants defender")
    void laterFlyingGrantApplies() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ConcordiaPegasus());
        harness.setHand(player1, List.of(new SkyTether(), new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Sky Tether removes flying granted before it enters")
    void removesEarlierFlyingGrant() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ConcordiaPegasus());
        harness.setHand(player1, List.of(new ArrestersZeal(), new SkyTether()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();
    }
}
