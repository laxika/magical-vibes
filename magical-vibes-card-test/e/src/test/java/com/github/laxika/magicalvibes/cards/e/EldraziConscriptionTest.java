package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.c.CaravanEscort;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldraziConscription.class, GlorySeeker.class, CaravanEscort.class, PropheticPrism.class})
class EldraziConscriptionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +10/+10 and trample")
    void enchantedCreatureGetsBoostAndTrample() {
        Permanent bears = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EldraziConscription());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Attacking with the enchanted creature makes the defending player sacrifice two permanents")
    void attackTriggersAnnihilator() {
        Permanent bears = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EldraziConscription());
        aura.setAttachedTo(bears.getId());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.addToBattlefield(player2, new CaravanEscort());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertNotOnBattlefield(player2, "Caravan Escort");
    }

    @Test
    @DisplayName("Removing Eldrazi Conscription removes its bonuses")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EldraziConscription());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Eldrazi Conscription cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new EldraziConscription()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Conscription can resolve attached to an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new EldraziConscription()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 8);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Eldrazi Conscription").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature's controller controls annihilator even with an opposing Aura")
    void opposingAuraGrantsAttackTriggerToCreature() {
        Permanent creature = addCreatureReady(player2, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EldraziConscription());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new PropheticPrism());

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Eldrazi Conscription");
        harness.assertNotOnBattlefield(player1, "Prophetic Prism");
        harness.assertOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Annihilator still resolves after the Aura leaves the battlefield")
    void attackTriggerSurvivesAuraRemoval() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EldraziConscription());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new CaravanEscort());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        harness.assertNotOnBattlefield(player2, "Caravan Escort");
    }

    @Test
    @DisplayName("A defending player with only one permanent sacrifices it")
    void sacrificesAllWhenFewerThanTwoPermanents() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EldraziConscription());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player2, new PropheticPrism());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The defending player chooses which two permanents to sacrifice")
    void defenderChoosesTwoPermanents() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EldraziConscription());
        aura.setAttachedTo(creature.getId());
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        Permanent escort = harness.addToBattlefieldAndReturn(player2, new CaravanEscort());
        harness.addToBattlefield(player2, new GlorySeeker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(prism.getId(), escort.getId()));

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        harness.assertNotOnBattlefield(player2, "Caravan Escort");
        harness.assertOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Removing the Aura before attacking removes annihilator")
    void auraRemovalPreventsAttackTrigger() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EldraziConscription());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new CaravanEscort());
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        harness.assertOnBattlefield(player2, "Caravan Escort");
    }
}
