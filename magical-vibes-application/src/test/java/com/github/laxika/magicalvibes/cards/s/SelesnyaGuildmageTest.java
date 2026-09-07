package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelesnyaGuildmage.class, GrizzlyBears.class})
class SelesnyaGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability creates a 1/1 green Saproling token")
    void firstAbilityCreatesSaprolingToken() {
        addReadyGuildmage(player1);
        addMana(ManaColor.COLORLESS, 3);
        addMana(ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability boosts creatures you control until end of turn")
    void secondAbilityBoostsOwnCreaturesUntilEndOfTurn() {
        addReadyGuildmage(player1);
        Permanent ownBears = addReady(player1, new GrizzlyBears());
        Permanent opposingBears = addReady(player2, new GrizzlyBears());
        addMana(ManaColor.COLORLESS, 3);
        addMana(ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
    }

    private Permanent addReadyGuildmage(Player player) {
        return addReady(player, new SelesnyaGuildmage());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }
}
