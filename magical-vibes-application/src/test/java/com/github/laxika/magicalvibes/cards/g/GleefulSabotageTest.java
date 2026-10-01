package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlightSickle;
import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.cards.j.JuvenileGloomwidow;
import com.github.laxika.magicalvibes.cards.r.RakingCanopy;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GleefulSabotage.class, BlightSickle.class, RakingCanopy.class,
        JuvenileGloomwidow.class, Cinderbones.class})
class GleefulSabotageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvesAndDestroysArtifact() {
        harness.addToBattlefield(player2, new BlightSickle());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Blight Sickle");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Blight Sickle");
        harness.assertInGraveyard(player2, "Blight Sickle");
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesAndDestroysEnchantment() {
        harness.addToBattlefield(player2, new RakingCanopy());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Raking Canopy");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Raking Canopy");
        harness.assertInGraveyard(player2, "Raking Canopy");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new Cinderbones());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        harness.addToBattlefield(player2, new BlightSickle());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent creature1 = addCreatureReady(player1, new JuvenileGloomwidow());
        Permanent creature2 = addCreatureReady(player1, new JuvenileGloomwidow());

        UUID targetId = harness.getPermanentId(player2, "Blight Sickle");
        harness.castWithConspire(player1, 0, targetId, List.of(creature1.getId(), creature2.getId()));

        GameData gd = harness.getGameData();
        assertThat(creature1.isTapped()).isTrue();
        assertThat(creature2.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Conspire is rejected when a chosen creature does not share a color with the spell")
    void conspireRejectsNonGreenCreature() {
        harness.addToBattlefield(player2, new BlightSickle());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent greenCreature = addCreatureReady(player1, new JuvenileGloomwidow());
        Permanent blackCreature = addCreatureReady(player1, new Cinderbones());

        UUID targetId = harness.getPermanentId(player2, "Blight Sickle");
        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, targetId,
                List.of(greenCreature.getId(), blackCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Conspire copy can choose a new artifact or enchantment target")
    void conspireCopyCanChooseNewTarget() {
        harness.addToBattlefield(player2, new BlightSickle());
        harness.addToBattlefield(player2, new RakingCanopy());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent creature1 = addCreatureReady(player1, new JuvenileGloomwidow());
        Permanent creature2 = addCreatureReady(player1, new JuvenileGloomwidow());
        UUID originalTargetId = harness.getPermanentId(player2, "Blight Sickle");
        UUID copyTargetId = harness.getPermanentId(player2, "Raking Canopy");

        harness.castWithConspire(player1, 0, originalTargetId,
                List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTargetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Blight Sickle");
        harness.assertInGraveyard(player2, "Blight Sickle");
        harness.assertNotOnBattlefield(player2, "Raking Canopy");
        harness.assertInGraveyard(player2, "Raking Canopy");
    }
}
