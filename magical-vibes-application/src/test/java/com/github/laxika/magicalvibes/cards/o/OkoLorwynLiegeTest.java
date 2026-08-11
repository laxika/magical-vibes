package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OkoLorwynLiegeTest extends BaseCardTest {

    @Test
    void frontFaceTransformsAfterPayingGreenInFirstMainPhase() {
        Permanent oko = addFrontFace(player1, 3);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(oko.isTransformed()).isTrue();
    }

    @Test
    void backFaceTransformsAfterPayingBlueInFirstMainPhase() {
        Permanent oko = addBackFace(player1, 3);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(oko.isTransformed()).isFalse();
    }

    @Test
    void frontFaceLoyaltyAbilitiesWork() {
        Permanent oko = addFrontFace(player1, 3);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        int okoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oko);
        harness.activateAbility(player1, okoIndex, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.CHANGELING)).isTrue();

        int powerBeforeMinusOne = gqs.getEffectivePower(gd, target);
        oko.setLoyaltyActivationsThisTurn(0);
        harness.activateAbility(player1, okoIndex, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBeforeMinusOne - 2);
    }

    @Test
    void millsAndOffersOnlyPermanentCards() {
        Forest forest = new Forest();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, shock, bears));
        Permanent oko = addBackFace(player1, 3);

        int okoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oko);
        harness.activateAbility(player1, okoIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(bears, shock)
                .doesNotContain(forest);
    }

    @Test
    void createsTwoElkTokens() {
        Permanent oko = addBackFace(player1, 3);

        int okoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oko);
        harness.activateAbility(player1, okoIndex, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> elks = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ELK))
                .toList();
        assertThat(elks).hasSize(2);
        assertThat(elks).allSatisfy(elk -> {
            assertThat(elk.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(elk.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(elk.getEffectivePower()).isEqualTo(3);
            assertThat(elk.getEffectiveToughness()).isEqualTo(3);
        });
    }

    @Test
    void emblemBoostsChosenCreatureType() {
        Permanent oko = addBackFace(player1, 6);
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int elfPowerBefore = gqs.getEffectivePower(gd, elf);
        int elfToughnessBefore = gqs.getEffectiveToughness(gd, elf);
        int bearPowerBefore = gqs.getEffectivePower(gd, bear);

        int okoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oko);
        harness.activateAbility(player1, okoIndex, 2, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        assertThat(gd.emblems).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(elfPowerBefore + 3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(elfToughnessBefore + 3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPowerBefore);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addFrontFace(Player player, int loyalty) {
        OkoLorwynLiege card = new OkoLorwynLiege();
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addBackFace(Player player, int loyalty) {
        OkoLorwynLiege card = new OkoLorwynLiege();
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        permanent.setTransformed(true);
        permanent.setCard(card.getBackFaceCard());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
