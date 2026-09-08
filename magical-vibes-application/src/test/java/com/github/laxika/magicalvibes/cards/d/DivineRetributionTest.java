package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivineRetribution.class, EkunduGriffin.class, FemerefScouts.class})
class DivineRetributionTest extends BaseCardTest {

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void prepareCast() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineRetribution()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);
    }

    @Test
    @DisplayName("Deals damage equal to the number of attacking creatures")
    void dealsDamageEqualToAttackerCount() {
        Permanent target = addAttacker(new FemerefScouts());
        addAttacker(new FemerefScouts());
        addAttacker(new FemerefScouts());

        prepareCast();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Divine Retribution");
    }

    @Test
    @DisplayName("A lone attacker takes only 1 damage and survives")
    void loneAttackerTakesOneDamage() {
        Permanent target = addAttacker(new FemerefScouts());

        prepareCast();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Femeref Scouts");
    }

    @Test
    @DisplayName("Lethal damage destroys the attacking creature")
    void lethalDamageDestroysAttacker() {
        Permanent target = addAttacker(new EkunduGriffin());
        addAttacker(new FemerefScouts());

        prepareCast();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ekundu Griffin");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addAttacker(new FemerefScouts());
        harness.addToBattlefield(player2, new FemerefScouts());
        UUID targetId = harness.getPermanentId(player2, "Femeref Scouts");

        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addAttacker(new FemerefScouts());

        prepareCast();
        harness.castInstant(player2, 0, target.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Divine Retribution");
    }

    @Test
    @DisplayName("Counts attacking creatures as the spell resolves")
    void countsAttackingCreaturesAtResolution() {
        Permanent target = addAttacker(new FemerefScouts());
        Permanent removedAttacker = addAttacker(new FemerefScouts());
        addAttacker(new FemerefScouts());

        prepareCast();
        harness.castInstant(player2, 0, target.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(removedAttacker);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }
}
