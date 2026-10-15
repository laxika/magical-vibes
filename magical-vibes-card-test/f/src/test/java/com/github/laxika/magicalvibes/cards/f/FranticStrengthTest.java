package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BearTrap;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FranticStrength.class, FearOfSurveillance.class, BearTrap.class})
class FranticStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Frantic Strength can be cast during the opponent's turn because of flash")
    void canBeCastDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearOfSurveillance());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        setHandAndMana();
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Frantic Strength gives the enchanted creature +2/+2 and trample")
    void boostsAndGrantsTrample() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FearOfSurveillance());

        setHandAndMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Removing Frantic Strength removes its bonuses")
    void effectsStopWhenRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FearOfSurveillance());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FranticStrength());
        aura.setAttachedTo(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Frantic Strength fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FearOfSurveillance());
        FranticStrength aura = new FranticStrength();

        harness.setHand(player1, List.of(aura));
        addCastingMana();
        harness.castEnchantment(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof FranticStrength);
    }

    @Test
    @DisplayName("Frantic Strength cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BearTrap());

        setHandAndMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Frantic Strength enchants an opponent's creature without boosting other creatures")
    void enchantsOpponentsCreatureOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearOfSurveillance());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FearOfSurveillance());

        setHandAndMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FranticStrength)
                .findFirst().orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Two copies stack their boosts and removing one leaves the other active")
    void multipleCopiesStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FearOfSurveillance());

        setHandAndMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        setHandAndMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FranticStrength)
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Frantic Strength goes to its owner's graveyard when the enchanted creature leaves")
    void auraDiesWhenEnchantedCreatureLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearOfSurveillance());
        FranticStrength aura = new FranticStrength();
        harness.setHand(player1, List.of(aura));
        addCastingMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
        harness.assertNotOnBattlefield(player1, "Frantic Strength");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(aura);
    }

    private void setHandAndMana() {
        harness.setHand(player1, List.of(new FranticStrength()));
        addCastingMana();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
