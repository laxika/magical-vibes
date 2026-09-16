package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.t.TrainedPronghorn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnquestionedAuthority.class, KrosanVerge.class, TrainedPronghorn.class})
class UnquestionedAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Unquestioned Authority attaches it and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TrainedPronghorn());
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.setLibrary(player1, List.of(new TrainedPronghorn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Unquestioned Authority")
                        && creature.getId().equals(permanent.getAttachedTo()));
        harness.assertInHand(player1, "Trained Pronghorn");
    }

    @Test
    @DisplayName("Enchanted creature can't be blocked by a creature")
    void creaturesCannotBlockEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new TrainedPronghorn());
        creature.setAttacking(true);
        enchant(creature);
        Permanent blocker = addCreatureReady(player2, new TrainedPronghorn());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Protection is lost when Unquestioned Authority leaves the battlefield")
    void protectionStopsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new TrainedPronghorn());
        Permanent aura = enchant(creature);
        Permanent attacker = addCreatureReady(player2, new TrainedPronghorn());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, attacker)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, attacker)).isFalse();
    }

    @Test
    @DisplayName("Protection applies to creature sources but not noncreature sources")
    void protectionOnlyAppliesToCreatureSources() {
        Permanent creature = addCreatureReady(player1, new TrainedPronghorn());
        enchant(creature);
        Permanent creatureSource = addCreatureReady(player2, new TrainedPronghorn());
        Permanent noncreatureSource = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, creatureSource)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, noncreatureSource)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new KrosanVerge());
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent land = findPermanent(player1, "Krosan Verge");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent enchant(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnquestionedAuthority());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
