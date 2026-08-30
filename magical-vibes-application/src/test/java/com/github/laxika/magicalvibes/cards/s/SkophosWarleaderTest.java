package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkophosWarleader.class, GrizzlyBears.class, GloriousAnthem.class})
class SkophosWarleaderTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndGainsPowerAndMenace() {
        Permanent warleader = addReadyWarlord();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId(), enchantment.getId());
        assertThat(choice.validIds()).doesNotContain(warleader.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(warleader.getPowerModifier()).isEqualTo(1);
        assertThat(warleader.getToughnessModifier()).isZero();
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    void sacrificesAnEnchantment() {
        Permanent warleader = addReadyWarlord();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glorious Anthem");
        assertThat(warleader.getPowerModifier()).isEqualTo(1);
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    void boostAndMenaceWearOffAtEndOfTurn() {
        Permanent warleader = addReadyWarlord();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warleader.getPowerModifier()).isZero();
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    void cannotActivateWithoutAnotherCreatureOrEnchantment() {
        Permanent warleader = addReadyWarlord();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(warleader), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private Permanent addReadyWarlord() {
        Permanent warleader = harness.addToBattlefieldAndReturn(player1, new SkophosWarleader());
        warleader.setSummoningSick(false);
        return warleader;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
