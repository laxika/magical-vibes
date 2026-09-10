package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed({HuntingPack.class, GrizzlyBears.class})
class HuntingPackTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates a 4/4 Beast token")
    void createsBeastToken() {
        castHuntingPack();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Beast");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(4);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Hunting Pack")
    void stormCopiesForEachPriorSpell() {
        GameData gd = harness.getGameData();
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        castHuntingPack();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Beast")).hasSize(3);
    }

    private void castHuntingPack() {
        harness.setHand(player1, List.of(new HuntingPack()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0);
    }
}
