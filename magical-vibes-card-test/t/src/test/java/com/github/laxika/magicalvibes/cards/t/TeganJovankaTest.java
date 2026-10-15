package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeganJovanka.class, Ornithopter.class, GrizzlyBears.class})
class TeganJovankaTest extends BaseCardTest {

    @Test
    @DisplayName("Tegan targets an attacking historic creature and grants both benefits")
    void targetsAttackingHistoricCreature() {
        addCreatureReady(player1, new TeganJovanka());
        Permanent historicAttacker = addCreatureReady(player1, new Ornithopter());
        Permanent nonHistoricAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .containsExactly(historicAttacker.getId())
                .doesNotContain(nonHistoricAttacker.getId());

        harness.handlePermanentChosen(player1, historicAttacker.getId());
        harness.passBothPriorities();

        assertThat(historicAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(historicAttacker.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, historicAttacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(nonHistoricAttacker.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nonHistoricAttacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Tegan's benefits wear off at end of turn")
    void benefitsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new TeganJovanka());
        Permanent historicAttacker = addCreatureReady(player1, new Ornithopter());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, historicAttacker.getId());
        harness.passBothPriorities();

        assertThat(historicAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, historicAttacker, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(historicAttacker.getPowerModifier()).isZero();
        assertThat(historicAttacker.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, historicAttacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Tegan can target herself when attacking, but not an idle historic creature")
    void canTargetHerselfWhenAttacking() {
        Permanent tegan = addCreatureReady(player1, new TeganJovanka());
        addCreatureReady(player1, new Ornithopter());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(tegan.getId());

        harness.handlePermanentChosen(player1, tegan.getId());
        harness.passBothPriorities();

        assertThat(tegan.getPowerModifier()).isEqualTo(1);
        assertThat(tegan.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, tegan, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("An attack with no historic attacker grants no benefits and requires no target choice")
    void noHistoricAttacker() {
        Permanent tegan = addCreatureReady(player1, new TeganJovanka());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(tegan.getPowerModifier()).isZero();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, tegan, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Tegan does not trigger when her opponent attacks")
    void opponentAttackDoesNotTrigger() {
        addCreatureReady(player1, new TeganJovanka());
        Permanent attacker = addCreatureReady(player2, new Ornithopter());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Tegan does not trigger when no attackers are declared")
    void noAttackersDoesNotTrigger() {
        Permanent tegan = addCreatureReady(player1, new TeganJovanka());

        declareAttackers(player1, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(tegan.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, tegan, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
