package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({UnquestionedAuthority.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class})
class UnquestionedAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Unquestioned Authority attaches it and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Unquestioned Authority")
                        && bears.getId().equals(permanent.getAttachedTo()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Enchanted creature can't be blocked by a creature")
    void creaturesCannotBlockEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        enchant(bears);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = enchant(bears);
        Permanent attacker = addCreatureReady(player2, new HillGiant());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, bears, attacker)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, bears, attacker)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent enchant(Permanent creature) {
        Permanent aura = new Permanent(new UnquestionedAuthority());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
