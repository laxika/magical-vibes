package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Compost;
import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.Donate;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.cards.p.PatternOfRebirth;
import com.github.laxika.magicalvibes.cards.r.RecklessAbandon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        AuraThief.class,
        Compost.class,
        Confiscate.class,
        Disenchant.class,
        Donate.class,
        MetathranSoldier.class,
        PatternOfRebirth.class,
        RecklessAbandon.class
})
class AuraThiefTest extends BaseCardTest {

    @Test
    @DisplayName("When Aura Thief dies, its controller gains control of all enchantments")
    void gainsControlOfAllEnchantmentsWhenItDies() {
        Permanent auraThief = addCreatureReady(player1, new AuraThief());
        Permanent creature = addCreatureReady(player2, new MetathranSoldier());
        Permanent compost = harness.addToBattlefieldAndReturn(player2, new Compost());
        Permanent patternOfRebirth = harness.addToBattlefieldAndReturn(player2, new PatternOfRebirth());
        patternOfRebirth.setAttachedTo(creature.getId());

        destroyWithRecklessAbandon(player1, auraThief);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(compost, patternOfRebirth);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(creature)
                .doesNotContain(compost, patternOfRebirth);
        assertThat(patternOfRebirth.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Aura Thief does not gain control of creatures")
    void doesNotGainControlOfCreatures() {
        Permanent auraThief = addCreatureReady(player1, new AuraThief());
        Permanent creature = addCreatureReady(player2, new MetathranSoldier());

        destroyWithRecklessAbandon(player1, auraThief);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("The controller, rather than the owner, gains control of enchantments")
    void controllerGainsControlAfterAuraThiefChangesControllers() {
        Permanent auraThief = addCreatureReady(player1, new AuraThief());
        Permanent creature = addCreatureReady(player1, new MetathranSoldier());
        Permanent compost = harness.addToBattlefieldAndReturn(player1, new Compost());
        Permanent patternOfRebirth = harness.addToBattlefieldAndReturn(player1, new PatternOfRebirth());
        patternOfRebirth.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), auraThief.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(auraThief);

        destroyWithRecklessAbandon(player1, auraThief);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(compost, patternOfRebirth);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(creature)
                .doesNotContain(compost, patternOfRebirth);
        assertThat(patternOfRebirth.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Aura Thief gains enchantments present at resolution, not ones entering afterward")
    void affectsOnlyEnchantmentsPresentWhenTriggerResolves() {
        Permanent auraThief = addCreatureReady(player1, new AuraThief());
        destroyWithRecklessAbandon(player1, auraThief);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(auraThief);
        assertThat(gd.stack).hasSize(1);
        Permanent enchantmentBeforeResolution = harness.addToBattlefieldAndReturn(player2, new Compost());

        resolveAllTriggers();
        Permanent enchantmentAfterResolution = harness.addToBattlefieldAndReturn(player2, new Compost());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(enchantmentBeforeResolution)
                .doesNotContain(enchantmentAfterResolution);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(enchantmentAfterResolution)
                .doesNotContain(enchantmentBeforeResolution);
    }

    @Test
    @DisplayName("Aura Thief keeps an already-controlled enchantment after Confiscate leaves")
    void keepsAlreadyControlledEnchantmentAfterOlderControlEffectEnds() {
        Permanent auraThief = addCreatureReady(player1, new AuraThief());
        Permanent compost = harness.addToBattlefieldAndReturn(player2, new Compost());

        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, compost.getId());
        resolveAllTriggers();

        Permanent confiscate = findPermanent(player1, "Confiscate");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(compost);

        destroyWithRecklessAbandon(player1, auraThief);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, confiscate.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(compost);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(compost);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(confiscate);
    }

    private void destroyWithRecklessAbandon(Player controller, Permanent target) {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(controller, new MetathranSoldier());
        harness.setHand(controller, List.of(new RecklessAbandon()));
        harness.addMana(controller, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(controller, 0, target.getId(), sacrifice.getId());
    }
}
