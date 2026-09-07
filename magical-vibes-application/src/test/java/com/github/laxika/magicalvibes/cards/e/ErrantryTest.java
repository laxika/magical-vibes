package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Errantry.class, GrizzlyBears.class, FountainOfYouth.class})
class ErrantryTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+0")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachErrantry(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Errantry can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Errantry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(aura -> aura.getCard() instanceof Errantry
                        && bears.getId().equals(aura.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature can attack alone")
    void canAttackAlone() {
        harness.setLife(player2, 20);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachErrantry(bears);

        declareAttackers(List.of(0));

        // Grizzly Bears (2/2) with Errantry (+3/+0) = 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Enchanted creature can't attack alongside another creature")
    void cannotAttackWithAnother() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachErrantry(bears);
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only attack alone");
    }

    @Test
    @DisplayName("An unenchanted creature can still attack alongside others")
    void otherCreaturesUnaffected() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        // Two unenchanted Grizzly Bears (2/2 each) = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Errantry")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Errantry()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Errantry's effects end when it becomes unattached")
    void effectsEndWhenUnattached() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachErrantry(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        aura.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThatCode(() -> declareAttackers(List.of(0, 1))).doesNotThrowAnyException();
    }

    private Permanent attachErrantry(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Errantry());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
