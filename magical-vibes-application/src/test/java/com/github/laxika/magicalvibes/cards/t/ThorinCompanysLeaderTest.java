package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({ThorinCompanysLeader.class, GrizzlyBears.class})
class ThorinCompanysLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("A Dwarf dealing combat damage creates two Treasures")
    void dwarfCombatDamageCreatesTwoTreasures() {
        addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent dwarf = addCreatureReady(player1, creature("Dwarf", CardSubtype.DWARF));
        dwarf.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Non-Dwarf combat damage does not create Treasures")
    void nonDwarfCombatDamageDoesNotCreateTreasures() {
        addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The activated ability grants own creatures double strike until end of turn")
    void activatedAbilityGrantsDoubleStrikeUntilEndOfTurn() {
        Permanent thorin = addCreatureReady(player1, new ThorinCompanysLeader());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thorin, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thorin, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
