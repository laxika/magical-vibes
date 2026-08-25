package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({FloatingShield.class, GrizzlyBears.class, FountainOfYouth.class})
class FloatingShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color gives the enchanted creature protection from it")
    void enchantedCreatureHasProtectionFromChosenColor() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FloatingShield()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the Aura grants its chosen-color protection to the target creature")
    void sacrificeGrantsChosenColorProtection() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = new Permanent(new FloatingShield());
        shield.setAttachedTo(host.getId());
        shield.setChosenColor(CardColor.RED);
        gd.playerBattlefields.get(player1.getId()).add(shield);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        harness.assertInGraveyard(player1, "Floating Shield");
    }

    @Test
    @DisplayName("Sacrifice protection wears off at end of turn")
    void sacrificeProtectionWearsOff() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = new Permanent(new FloatingShield());
        shield.setAttachedTo(host.getId());
        shield.setChosenColor(CardColor.BLUE);
        gd.playerBattlefields.get(player1.getId()).add(shield);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("The sacrifice ability cannot target a noncreature permanent")
    void sacrificeAbilityCannotTargetNoncreature() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = new Permanent(new FloatingShield());
        shield.setAttachedTo(host.getId());
        shield.setChosenColor(CardColor.GREEN);
        gd.playerBattlefields.get(player1.getId()).add(shield);
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanent(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
