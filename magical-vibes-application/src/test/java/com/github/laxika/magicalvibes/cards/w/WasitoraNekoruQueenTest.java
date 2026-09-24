package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WasitoraNekoruQueen.class, GrizzlyBears.class})
class WasitoraNekoruQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player sacrifice a creature")
    void damagedPlayerSacrificesCreature() {
        Permanent wasitora = addCreatureReady(player1, new WasitoraNekoruQueen());
        wasitora.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Cat Dragon")).isZero();
    }

    @Test
    @DisplayName("Creates a Cat Dragon when the damaged player has no creature to sacrifice")
    void createsTokenWithoutCreatureToSacrifice() {
        Permanent wasitora = addCreatureReady(player1, new WasitoraNekoruQueen());
        wasitora.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Cat Dragon");
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("The damaged player chooses which creature to sacrifice")
    void damagedPlayerChoosesCreature() {
        Permanent wasitora = addCreatureReady(player1, new WasitoraNekoruQueen());
        wasitora.setAttacking(true);
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Cat Dragon")).isZero();
    }
}
