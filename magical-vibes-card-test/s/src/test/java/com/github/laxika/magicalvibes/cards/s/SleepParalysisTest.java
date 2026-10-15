package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.v.VeilOfSummer;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleepParalysis.class, Vorstclaw.class, GrafdiggersCage.class, VeilOfSummer.class})
class SleepParalysisTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sleep Paralysis taps and enchants the target creature")
    void resolvingTapsAndEnchantsTarget() {
        Permanent creature = addCreatureReady(player2, new Vorstclaw());

        harness.setHand(player1, List.of(new SleepParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sleep Paralysis")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new Vorstclaw());
        creature.tap();

        Permanent aura = new Permanent(new SleepParalysis());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other creatures still untap normally")
    void otherCreaturesStillUntap() {
        Permanent enchantedCreature = addCreatureReady(player2, new Vorstclaw());
        enchantedCreature.tap();
        Permanent otherCreature = addCreatureReady(player2, new Vorstclaw());
        otherCreature.tap();

        Permanent aura = new Permanent(new SleepParalysis());
        aura.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.performUntapStep(player2);

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature untaps after Sleep Paralysis is removed")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new Vorstclaw());
        creature.tap();

        Permanent aura = new Permanent(new SleepParalysis());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sleep Paralysis cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setHand(player1, List.of(new SleepParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        Permanent artifact = findPermanent(player1, "Grafdigger's Cage");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Sleep Paralysis fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new Vorstclaw());

        harness.setHand(player1, List.of(new SleepParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sleep Paralysis");
        harness.assertNotOnBattlefield(player1, "Sleep Paralysis");
    }

    @Test
    @DisplayName("Entry trigger taps the enchanted creature even if it gains hexproof from blue")
    void entryTriggerDoesNotTargetEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new Vorstclaw());
        harness.setHand(player1, List.of(new SleepParalysis()));
        harness.setHand(player2, List.of(new VeilOfSummer()));
        harness.setLibrary(player2, List.of(new Vorstclaw()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sleep Paralysis");
        assertThat(creature.isTapped()).isFalse();

        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Sleep Paralysis");
    }

    @Test
    @DisplayName("Sleep Paralysis can enchant its controller's own creature")
    void canEnchantOwnCreature() {
        Permanent creature = addCreatureReady(player1, new Vorstclaw());
        harness.setHand(player1, List.of(new SleepParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Sleep Paralysis").getAttachedTo())
                .isEqualTo(creature.getId());
    }
}
