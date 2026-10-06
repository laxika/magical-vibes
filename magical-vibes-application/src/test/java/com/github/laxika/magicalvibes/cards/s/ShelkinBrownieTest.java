package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdventurersGuildhouse;
import com.github.laxika.magicalvibes.cards.b.BatonOfMorale;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.l.LivonyaSilone;
import com.github.laxika.magicalvibes.cards.m.MasterOfTheHunt;
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

@CardUsed({ShelkinBrownie.class, MasterOfTheHunt.class, BatonOfMorale.class, Karakas.class,
        AdventurersGuildhouse.class, LivonyaSilone.class})
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

    @Test
    @DisplayName("Can target itself even without bands with other")
    void canTargetItselfWithoutBandsWithOther() {
        Permanent brownie = addCreatureReady(player1, new ShelkinBrownie());

        harness.activateAbility(player1, 0, null, brownie.getId());
        harness.passBothPriorities();

        assertThat(brownie.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brownie);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent brownie = harness.addToBattlefieldAndReturn(player1, new ShelkinBrownie());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, brownie.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(brownie.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent brownie = addCreatureReady(player1, new ShelkinBrownie());
        brownie.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, brownie.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removes bands with other granted by an existing Guildhouse")
    void removesAbilityGrantedByExistingGuildhouse() {
        addCreatureReady(player1, new ShelkinBrownie());
        Permanent legend = addCreatureReady(player1, new LivonyaSilone());
        harness.enterBattlefieldAndReturn(player1, new AdventurersGuildhouse());
        assertThat(gqs.canUseBandsWithOther(gd, List.of(legend), false)).isTrue();

        harness.activateAbility(player1, 0, null, legend.getId());
        harness.passBothPriorities();

        assertThat(gqs.canUseBandsWithOther(gd, List.of(legend), false)).isFalse();
        assertThat(gqs.hasKeyword(gd, legend, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.canUseBandsWithOther(gd, List.of(legend), false)).isTrue();
    }

    @Test
    @DisplayName("A later Guildhouse can grant bands with other after Brownie resolves")
    void laterGuildhouseCanGrantAbilityAfterBrownieResolves() {
        addCreatureReady(player1, new ShelkinBrownie());
        Permanent legend = addCreatureReady(player1, new LivonyaSilone());

        harness.activateAbility(player1, 0, null, legend.getId());
        harness.passBothPriorities();
        assertThat(gqs.canUseBandsWithOther(gd, List.of(legend), false)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new AdventurersGuildhouse());

        assertThat(gqs.canUseBandsWithOther(gd, List.of(legend), false)).isTrue();
        assertThat(gqs.hasKeyword(gd, legend, Keyword.FIRST_STRIKE)).isTrue();
    }
}
