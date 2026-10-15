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

@CardUsed({Chatterstorm.class, GrizzlyBears.class, Counterspell.class})
class ChatterstormTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates a 1/1 green Squirrel token")
    void createsSquirrelToken() {
        castChatterstorm();

        resolveAllTriggers();

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

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(3);
    }

    @Test
    @DisplayName("Storm copies are not casts and do not increase later storm counts")
    void copiesDoNotIncreaseLaterStormCounts() {
        castChatterstorm();
        resolveAllTriggers();
        castChatterstorm();
        resolveAllTriggers();
        castChatterstorm();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(6);
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Chatterstorm).hasSize(3);
        assertThat(findPermanents(player2, "Squirrel")).isEmpty();
    }

    @Test
    @DisplayName("Storm survives countering the original and excludes spells cast afterward")
    void stormSurvivesCounteringOriginal() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        Chatterstorm chatterstorm = new Chatterstorm();
        harness.castFromHand(player1, chatterstorm, "{1}{G}");
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chatterstorm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chatterstorm);
        assertThat(findPermanents(player1, "Squirrel")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
        assertThat(findPermanents(player2, "Squirrel")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castChatterstorm() {
        harness.castFromHand(player1, new Chatterstorm(), "{1}{G}");
    }
}
