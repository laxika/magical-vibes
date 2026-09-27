package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BatonOfMorale;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.m.MasterOfTheHunt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShelkinBrownie.class, MasterOfTheHunt.class, BatonOfMorale.class, Karakas.class})
class ShelkinBrownieTest extends BaseCardTest {

    @Test
    @DisplayName("Removes only bands with other until end of turn")
    void removesOnlyBandsWithOtherUntilEndOfTurn() {
        addCreatureReady(player1, new MasterOfTheHunt());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Wolves of the Hunt");
        Permanent brownie = addCreatureReady(player1, new ShelkinBrownie());
        Permanent baton = harness.addToBattlefieldAndReturn(player1, new BatonOfMorale());

        int batonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(baton);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, batonIndex, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.bandsWithOtherNames(gd, wolf)).containsExactly("Wolves of the Hunt");
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.BANDING)).isTrue();

        int brownieIndex = gd.playerBattlefields.get(player1.getId()).indexOf(brownie);
        harness.activateAbility(player1, brownieIndex, null, wolf.getId());
        assertThat(brownie.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.bandsWithOtherNames(gd, wolf)).isEmpty();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.BANDING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.bandsWithOtherNames(gd, wolf)).containsExactly("Wolves of the Hunt");
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        addCreatureReady(player2, new MasterOfTheHunt());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player2, "Wolves of the Hunt");
        assertThat(gqs.bandsWithOtherNames(gd, wolf)).containsExactly("Wolves of the Hunt");
        addCreatureReady(player1, new ShelkinBrownie());
        int brownieIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;

        harness.activateAbility(player1, brownieIndex, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.bandsWithOtherNames(gd, wolf)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new ShelkinBrownie());
        Permanent karakas = harness.addToBattlefieldAndReturn(player2, new Karakas());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, karakas.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
