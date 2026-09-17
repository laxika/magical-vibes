package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Chatterstorm.class, GrizzlyBears.class})
class ChatterstormTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates a 1/1 green Squirrel token")
    void createsSquirrelToken() {
        castChatterstorm();

        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        assertThat(squirrels).hasSize(1);
        assertThat(squirrels.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(squirrels.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Storm copies Chatterstorm once for each spell cast before it this turn")
    void stormCopiesForEachPriorSpell() {
        GameData gd = harness.getGameData();
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        castChatterstorm();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(3);
    }

    private void castChatterstorm() {
        harness.setHand(player1, List.of(new Chatterstorm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
    }
}
