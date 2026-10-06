package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HarrierGriffin;
import com.github.laxika.magicalvibes.cards.o.OrzhovSignet;
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

@CardUsed({ShadowLance.class, HarrierGriffin.class, OrzhovSignet.class})
class ShadowLanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Shadow Lance attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent griffin = addCreatureReady(player1, new HarrierGriffin());
        harness.setHand(player1, List.of(new ShadowLance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, griffin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ShadowLance
                        && griffin.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature has first strike")
    void enchantedCreatureHasFirstStrike() {
        Permanent griffin = addCreatureReady(player1, new HarrierGriffin());
        addAttachedAura(griffin);

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Shadow Lance can activate the pump ability")
    void auraCanActivatePumpAbility() {
        Permanent griffin = addCreatureReady(player1, new HarrierGriffin());
        addAttachedAura(griffin);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(5);
    }

    @Test
    @DisplayName("The pump bonus wears off at cleanup")
    void pumpBonusWearsOffAtCleanup() {
        Permanent griffin = addCreatureReady(player1, new HarrierGriffin());
        addAttachedAura(griffin);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shadow Lance cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new OrzhovSignet());
        harness.setHand(player1, List.of(new ShadowLance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The Aura controller can pump an opponent's enchanted creature")
    void auraControllerCanPumpOpponentsCreature() {
        Permanent griffin = addCreatureReady(player2, new HarrierGriffin());
        harness.setHand(player1, List.of(new ShadowLance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, griffin.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(5);
    }

    @Test
    @DisplayName("The pump ability requires black mana")
    void pumpAbilityRequiresBlackMana() {
        Permanent griffin = addCreatureReady(player1, new HarrierGriffin());
        addAttachedAura(griffin);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shadow Lance's effects end when it leaves the battlefield")
    void effectsEndWhenAuraLeavesBattlefield() {
        Permanent griffin = addCreatureReady(player1, new HarrierGriffin());
        Permanent aura = addAttachedAura(griffin);

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FIRST_STRIKE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("An opponent cannot activate an ability through the enchanted creature")
    void opponentCannotActivatePumpThroughCreature() {
        Permanent griffin = addCreatureReady(player2, new HarrierGriffin());
        addAttachedAura(griffin);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Repeated Aura activations accumulate their bonuses")
    void repeatedActivationsAccumulate() {
        Permanent griffin = addCreatureReady(player1, new HarrierGriffin());
        addAttachedAura(griffin);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(7);
    }

    private Permanent addAttachedAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShadowLance());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }
}
