package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
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

@CardUsed({FloatingShield.class, AngelOfRetribution.class, CabalCoffers.class})
class FloatingShieldTest extends BaseCardTest {

    private Permanent attachShield(Permanent host, CardColor chosenColor) {
        Permanent shield = new Permanent(new FloatingShield());
        shield.setAttachedTo(host.getId());
        shield.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(shield);
        return shield;
    }

    @Test
    @DisplayName("Choosing a color gives the enchanted creature protection from it")
    void enchantedCreatureHasProtectionFromChosenColor() {
        Permanent angel = addCreatureReady(player1, new AngelOfRetribution());
        harness.setHand(player1, List.of(new FloatingShield()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, angel.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.hasProtectionFrom(gd, angel, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, angel, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection from the chosen color does not remove the Aura")
    void protectionDoesNotRemoveAura() {
        Permanent host = addCreatureReady(player1, new AngelOfRetribution());
        Permanent shield = attachShield(host, CardColor.WHITE);

        harness.runStateBasedActions();

        assertThat(gqs.hasProtectionFrom(gd, host, CardColor.WHITE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shield);
        assertThat(shield.getAttachedTo()).isEqualTo(host.getId());
    }

    @Test
    @DisplayName("Sacrificing the Aura grants its chosen-color protection to the target creature")
    void sacrificeGrantsChosenColorProtection() {
        Permanent host = addCreatureReady(player1, new AngelOfRetribution());
        attachShield(host, CardColor.RED);
        Permanent target = addCreatureReady(player2, new AngelOfRetribution());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        harness.assertInGraveyard(player1, "Floating Shield");
    }

    @Test
    @DisplayName("Sacrifice protection wears off at end of turn")
    void sacrificeProtectionWearsOff() {
        Permanent host = addCreatureReady(player1, new AngelOfRetribution());
        attachShield(host, CardColor.BLUE);
        Permanent target = addCreatureReady(player2, new AngelOfRetribution());

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
        Permanent host = addCreatureReady(player1, new AngelOfRetribution());
        attachShield(host, CardColor.GREEN);
        harness.addToBattlefield(player2, new CabalCoffers());
        Permanent land = findPermanent(player2, "Cabal Coffers");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
