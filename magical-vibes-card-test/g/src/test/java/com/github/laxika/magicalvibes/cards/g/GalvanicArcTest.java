package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.InvasionOfVryn;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.OverloadedMageRing;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GalvanicArc.class, GiantSpider.class, HowlingMine.class, RuneclawBear.class,
        ChandraNalaar.class, Naturalize.class, Unsummon.class, InvasionOfVryn.class, OverloadedMageRing.class})
class GalvanicArcTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has first strike and the ETB trigger deals 3 damage to a player")
    void grantsFirstStrikeAndDamagesPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setLife(player2, 20);

        castGalvanicArc(creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The ETB trigger deals 3 damage to a creature")
    void damagesCreature() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        castGalvanicArc(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ETB trigger deals 3 damage to a planeswalker")
    void damagesPlaneswalker() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        castGalvanicArc(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ETB trigger may target the enchanted creature")
    void mayTargetEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        castGalvanicArc(creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ETB damage cannot target a noncreature artifact")
    void rejectsInvalidEtbTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        castGalvanicArc(creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The ETB trigger deals 3 damage to a battle")
    void damagesBattle() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfVryn());
        battle.setCounterCount(CounterType.DEFENSE, 4);

        castGalvanicArc(creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura removes first strike but does not stop its ETB damage")
    void triggerResolvesAfterAuraIsDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castGalvanicArc(creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Galvanic Arc"));

        harness.assertInGraveyard(player1, "Galvanic Arc");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An illegal enchant target prevents the Aura from entering and dealing damage")
    void doesNotTriggerWhenEnchantTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castGalvanicArc(creature.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Galvanic Arc");
        harness.assertNotOnBattlefield(player1, "Galvanic Arc");
        harness.assertInHand(player1, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Aura can enchant an opposing creature and its trigger can target its controller")
    void enchantsOpposingCreatureAndDamagesController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        castGalvanicArc(creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    private void castGalvanicArc(UUID enchantTargetId) {
        harness.setHand(player1, List.of(new GalvanicArc()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, enchantTargetId);
    }
}
