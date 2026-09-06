package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RockfaceVillage.class, LagacLizard.class, GrizzlyBears.class, Opt.class})
class RockfaceVillageTest extends BaseCardTest {

    @Test
    void tapsForColorless() {
        addVillage();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void addsRedManaOnlyForCreatureSpells() {
        addVillage();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    void creatureOnlyRedManaCannotCastNoncreatureSpells() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new Opt()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void boostsQualifyingCreatureAndGrantsHasteUntilEndOfTurn() {
        addVillage();
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new LagacLizard());
        int originalPower = lizard.getEffectivePower();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, lizard.getId());
        harness.passBothPriorities();

        assertThat(lizard.getEffectivePower()).isEqualTo(originalPower + 1);
        assertThat(gqs.hasKeyword(gd, lizard, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lizard.getEffectivePower()).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, lizard, Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotTargetNonKindredCreature() {
        addVillage();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateBoostAbilityOutsideSorcerySpeed() {
        addVillage();
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new LagacLizard());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, lizard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addVillage() {
        harness.addToBattlefield(player1, new RockfaceVillage());
    }
}
