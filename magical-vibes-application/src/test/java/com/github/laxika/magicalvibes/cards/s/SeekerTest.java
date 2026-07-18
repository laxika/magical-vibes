package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by a non-artifact non-white creature")
    void cannotBeBlockedByOrdinaryCreature() {
        enchantedAttacker();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact creatures and white creatures");
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a white creature")
    void canBeBlockedByWhiteCreature() {
        enchantedAttacker();

        Permanent vanguard = new Permanent(new EliteVanguard());
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(vanguard);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(vanguard.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        enchantedAttacker();

        Permanent thopter = new Permanent(new Ornithopter());
        thopter.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(thopter);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(thopter.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Seeker")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Seeker()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void enchantedAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent aura = new Permanent(new Seeker());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
