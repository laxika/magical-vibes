package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShizoDeathsStorehouseTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds {B}")
    void manaAbilityAddsBlack() {
        harness.addToBattlefield(player1, new ShizoDeathsStorehouse());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent shizo = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shizo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Grants fear to a legendary creature")
    void grantsFearToLegendaryCreature() {
        harness.addToBattlefield(player1, new ShizoDeathsStorehouse());
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID mirriId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        harness.activateAbility(player1, 0, 1, null, mirriId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, permanent(mirriId), Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Fear wears off at end of turn")
    void fearWearsOff() {
        harness.addToBattlefield(player1, new ShizoDeathsStorehouse());
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID mirriId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        harness.activateAbility(player1, 0, 1, null, mirriId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, permanent(mirriId), Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("A nonlegendary creature is not a legal target")
    void nonlegendaryCreatureIsIllegalTarget() {
        harness.addToBattlefield(player1, new ShizoDeathsStorehouse());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent permanent(UUID id) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(id)).findFirst().orElseThrow();
    }
}
