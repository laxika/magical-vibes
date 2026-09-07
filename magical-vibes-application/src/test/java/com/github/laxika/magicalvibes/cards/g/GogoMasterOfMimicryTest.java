package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RingsOfBrighthearth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GogoMasterOfMimicry.class, ProdigalPyromancer.class, RingsOfBrighthearth.class,
        Shock.class, TrialOfZeal.class})
class GogoMasterOfMimicryTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a triggered ability X times")
    void copiesTriggeredAbilityXTimes() {
        harness.setLife(player2, 20);
        addReady(player1, new GogoMasterOfMimicry());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        UUID triggerCardId = abilityCardIdOnStack(StackEntryType.TRIGGERED_ABILITY);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 2, triggerCardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Copies an activated ability X times")
    void copiesActivatedAbilityXTimes() {
        harness.setLife(player2, 20);
        addReady(player1, new GogoMasterOfMimicry());
        addReady(player1, new ProdigalPyromancer());

        int pyromancerIndex = findBattlefieldIndex(player1, "Prodigal Pyromancer");
        harness.activateAbility(player1, pyromancerIndex, null, player2.getId());
        UUID abilityCardId = abilityCardIdOnStack(StackEntryType.ACTIVATED_ABILITY);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 2, abilityCardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not allow X to be zero")
    void xCannotBeZero() {
        addReady(player1, new GogoMasterOfMimicry());
        addReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, findBattlefieldIndex(player1, "Prodigal Pyromancer"), null, player2.getId());
        UUID abilityCardId = abilityCardIdOnStack(StackEntryType.ACTIVATED_ABILITY);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 0, abilityCardId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must be at least 1");
    }

    @Test
    @DisplayName("Cannot be copied")
    void abilityCannotBeCopied() {
        harness.setLife(player2, 20);
        addReady(player1, new RingsOfBrighthearth());
        addReady(player1, new GogoMasterOfMimicry());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        UUID triggerCardId = abilityCardIdOnStack(StackEntryType.TRIGGERED_ABILITY);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 1, triggerCardId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotTargetSpell() {
        addReady(player1, new GogoMasterOfMimicry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        UUID spellCardId = gd.stack.getLast().getCard().getId();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 1, spellCardId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int findBattlefieldIndex(Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (name.equals(battlefield.get(i).getCard().getName())) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }

    private UUID abilityCardIdOnStack(StackEntryType type) {
        return gd.stack.stream()
                .filter(entry -> entry.getEntryType() == type)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
    }
}
