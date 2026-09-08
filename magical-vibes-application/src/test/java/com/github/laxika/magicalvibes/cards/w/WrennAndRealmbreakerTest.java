package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrennAndRealmbreaker.class, Forest.class, GrizzlyBears.class, Shock.class})
class WrennAndRealmbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Lands you control gain an ability to add one mana of any color")
    void landsGainAnyColorManaAbility() {
        addReadyWrenn(player1, 3);
        Permanent forest = addLand(player1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 animates up to one land with vigilance, hexproof, and haste until your next turn")
    void plusOneAnimatesTargetLand() {
        Permanent wrenn = addReadyWrenn(player1, 3);
        Permanent forest = addLand(player1);

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("+1 may be activated without choosing a land")
    void plusOneMayChooseNoTarget() {
        Permanent wrenn = addReadyWrenn(player1, 3);
        Permanent forest = addLand(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("-2 mills three cards and may return a milled permanent to hand")
    void minusTwoMillsAndReturnsPermanent() {
        Permanent wrenn = addReadyWrenn(player1, 3);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Shock(), new Shock()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("-7 grants an emblem to play lands and cast permanent spells from the graveyard")
    void minusSevenGrantsGraveyardPermission() {
        Permanent wrenn = addReadyWrenn(player1, 7);
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, bears));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
        assertThat(gd.emblems).hasSize(1);

        harness.playGraveyardLand(player1, gd.playerGraveyards.get(player1.getId()).indexOf(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, gd.playerGraveyards.get(player1.getId()).indexOf(bears));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyWrenn(Player player, int loyalty) {
        Permanent permanent = new Permanent(new WrennAndRealmbreaker());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addLand(Player player) {
        Permanent permanent = new Permanent(new Forest());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
