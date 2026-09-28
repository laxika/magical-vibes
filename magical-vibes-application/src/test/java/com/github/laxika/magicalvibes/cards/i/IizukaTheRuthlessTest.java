package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.g.GodosIrregulars;
import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IizukaTheRuthless.class, ArabaMothrider.class, GodosIrregulars.class, HandOfHonor.class})
class IizukaTheRuthlessTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido 2 triggers when Iizuka becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent iizuka = addCreatureReady(player1, new IizukaTheRuthless());
        addCreatureReady(player2, new GodosIrregulars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, iizuka)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, iizuka)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bushido 2 triggers when Iizuka blocks")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new GodosIrregulars());
        Permanent iizuka = addCreatureReady(player2, new IizukaTheRuthless());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, iizuka)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, iizuka)).isEqualTo(5);
    }

    @Test
    @DisplayName("Iizuka gets no Bushido bonus when it is unblocked")
    void unblockedGetsNoBushidoBonus() {
        Permanent iizuka = addCreatureReady(player1, new IizukaTheRuthless());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gqs.getEffectivePower(gd, iizuka)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, iizuka)).isEqualTo(3);
    }

    @Test
    @DisplayName("Iizuka's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent iizuka = addCreatureReady(player1, new IizukaTheRuthless());
        addCreatureReady(player2, new GodosIrregulars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, iizuka)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, iizuka)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, iizuka)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, iizuka)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing a Samurai grants double strike to own Samurai until end of turn")
    void sacrificeSamuraiGrantsDoubleStrikeUntilEndOfTurn() {
        Permanent iizuka = addCreatureReady(player1, new IizukaTheRuthless());
        Permanent sacrificedSamurai = addCreatureReady(player1, new ArabaMothrider());
        Permanent ownSamurai = addCreatureReady(player1, new HandOfHonor());
        Permanent nonSamurai = addCreatureReady(player1, new GodosIrregulars());
        Permanent opponentSamurai = addCreatureReady(player2, new HandOfHonor());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, iizuka), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .contains(iizuka.getId(), sacrificedSamurai.getId(), ownSamurai.getId())
                .doesNotContain(nonSamurai.getId(), opponentSamurai.getId());
        harness.handlePermanentChosen(player1, sacrificedSamurai.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, iizuka, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownSamurai, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSamurai, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSamurai, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Araba Mothrider");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, iizuka, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownSamurai, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Iizuka may be sacrificed as the Samurai cost")
    void maySacrificeItselfAsSamurai() {
        Permanent iizuka = addCreatureReady(player1, new IizukaTheRuthless());
        Permanent ownSamurai = addCreatureReady(player1, new HandOfHonor());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, iizuka), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, iizuka.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Iizuka the Ruthless");
        assertThat(gqs.hasKeyword(gd, ownSamurai, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
