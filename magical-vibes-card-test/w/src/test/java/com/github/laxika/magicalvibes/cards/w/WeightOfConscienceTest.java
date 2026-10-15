package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeightOfConscience.class, IndomitableAncients.class, PricklyBoggart.class, AmoeboidChangeling.class})
class WeightOfConscienceTest extends BaseCardTest {

    @Test
    @DisplayName("Creature enchanted with Weight of Conscience cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent enchanted = addCreatureReady(player1, new IndomitableAncients());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player2, new WeightOfConscience());
        auraPerm.setAttachedTo(enchanted.getId());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creature enchanted with Weight of Conscience can still block")
    void enchantedCreatureCanStillBlock() {
        Permanent attacker = addCreatureReady(player1, new IndomitableAncients());
        attacker.setAttacking(true);
        Permanent enchanted = addCreatureReady(player2, new IndomitableAncients());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new WeightOfConscience());
        auraPerm.setAttachedTo(enchanted.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(enchanted),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(enchanted.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapping two creatures that share a creature type exiles the enchanted creature")
    void activatedAbilityExilesEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new IndomitableAncients());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new WeightOfConscience());
        auraPerm.setAttachedTo(enchanted.getId());

        Permanent firstCreature = addCreatureReady(player1, new IndomitableAncients());
        Permanent secondCreature = addCreatureReady(player1, new IndomitableAncients());

        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(auraPerm);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(enchanted.getCard().getId()));
        harness.assertNotOnBattlefield(player1, "Weight of Conscience");
    }

    @Test
    @DisplayName("Ability can't be activated without two creatures sharing a creature type")
    void cannotActivateWithoutSharedType() {
        Permanent enchanted = addCreatureReady(player2, new IndomitableAncients());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new WeightOfConscience());
        auraPerm.setAttachedTo(enchanted.getId());

        Permanent treefolk = addCreatureReady(player1, new IndomitableAncients());
        Permanent boggart = addCreatureReady(player1, new PricklyBoggart());

        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(auraPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a creature type");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(treefolk.isTapped()).isFalse();
        assertThat(boggart.isTapped()).isFalse();
    }

    @Test
    void castingAuraAttachesItAndPreventsAttacking() {
        Permanent creature = addCreatureReady(player1, new IndomitableAncients());
        harness.setHand(player1, List.of(new WeightOfConscience()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Weight of Conscience").getAttachedTo()).isEqualTo(creature.getId());
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void summoningSickCreaturesCanPayTapCost() {
        Permanent enchanted = addCreatureReady(player2, new IndomitableAncients());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WeightOfConscience());
        aura.setAttachedTo(enchanted.getId());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IndomitableAncients());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IndomitableAncients());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchanted.getCard());
    }

    @Test
    void tappedCreatureCannotPayCostEvenWithMatchingType() {
        Permanent enchanted = addCreatureReady(player2, new IndomitableAncients());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WeightOfConscience());
        aura.setAttachedTo(enchanted.getId());
        Permanent first = addCreatureReady(player1, new IndomitableAncients());
        Permanent second = addCreatureReady(player1, new IndomitableAncients());
        second.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a creature type");
        assertThat(first.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
    }

    @Test
    void exilesCreatureEnchantedAtResolutionAfterAuraMoves() {
        Permanent original = addCreatureReady(player2, new IndomitableAncients());
        Permanent replacement = addCreatureReady(player2, new PricklyBoggart());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WeightOfConscience());
        aura.setAttachedTo(original.getId());
        addCreatureReady(player1, new IndomitableAncients());
        addCreatureReady(player1, new IndomitableAncients());

        harness.activateAbility(player1, 0, null, null);
        aura.setAttachedTo(replacement.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(original).doesNotContain(replacement);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(replacement.getCard()).doesNotContain(original.getCard());
    }

    @Test
    void creaturesThatLostTheirTypesCannotPaySharedTypeCost() {
        Permanent enchanted = addCreatureReady(player2, new IndomitableAncients());
        Permanent changeling = addCreatureReady(player2, new AmoeboidChangeling());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WeightOfConscience());
        aura.setAttachedTo(enchanted.getId());
        Permanent first = addCreatureReady(player1, new IndomitableAncients());
        Permanent second = addCreatureReady(player1, new IndomitableAncients());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(changeling),
                1, null, first.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a creature type");
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
    }
}
