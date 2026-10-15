package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElementalEruption.class, GrizzlyBears.class})
class ElementalEruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 flying Dragon Elemental token with prowess")
    void createsDragonElementalToken() {
        castElementalEruption();

        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Dragon Elemental");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.DRAGON, CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.PROWESS);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Elemental Eruption")
    void stormCopiesForEachPriorSpell() {
        GameData gd = harness.getGameData();
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        castElementalEruption();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dragon Elemental")).hasSize(3);
    }

    @Test
    @DisplayName("Dragon prowess triggers for a subsequent cast but not its storm copies")
    void prowessTriggersOnlyForCastSpell() {
        castElementalEruption();
        resolveAllTriggers();
        Permanent firstDragon = findPermanent(player1, "Dragon Elemental");

        castElementalEruption();
        resolveAllTriggers();

        assertThat(firstDragon.getEffectivePower()).isEqualTo(5);
        assertThat(firstDragon.getEffectiveToughness()).isEqualTo(5);
        assertThat(findPermanents(player1, "Dragon Elemental")).hasSize(3);
        assertThat(findPermanents(player1, "Dragon Elemental").stream()
                .filter(token -> token != firstDragon))
                .allSatisfy(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(4);
                    assertThat(token.getEffectiveToughness()).isEqualTo(4);
                });
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Storm counts are fixed when cast and exclude spells recorded afterward")
    void stormDoesNotCountLaterSpells() {
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        castElementalEruption();
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Dragon Elemental")).hasSize(2);
    }

    private void castElementalEruption() {
        harness.setHand(player1, List.of(new ElementalEruption()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
    }
}
