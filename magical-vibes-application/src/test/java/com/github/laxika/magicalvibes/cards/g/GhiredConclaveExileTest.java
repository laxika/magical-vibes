package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GhiredConclaveExile.class)
class GhiredConclaveExileTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 4/4 green Rhino creature token with trample")
    void entersWithRhinoToken() {
        harness.setHand(player1, List.of(new GhiredConclaveExile()));
        addGhiredMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent rhino = findPermanents(player1, "Rhino").getFirst();
        assertThat(rhino.getCard().isToken()).isTrue();
        assertThat(rhino.getEffectivePower()).isEqualTo(4);
        assertThat(rhino.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, rhino, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Populates a creature token when it attacks, and the copy enters tapped and attacking")
    void attackPopulatesTappedAndAttacking() {
        Permanent ghired = addCreatureReady(player1, new GhiredConclaveExile());
        harness.addToBattlefield(player1, rhinoToken());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> {
                    declareAttackers(List.of(0));
                    resolveAllTriggers();
                });

        List<Permanent> rhinos = findPermanents(player1, "Rhino");
        assertThat(rhinos).hasSize(2);
        Permanent attackingRhino = rhinos.stream().filter(Permanent::isAttacking).findFirst().orElseThrow();
        assertThat(attackingRhino.isTapped()).isTrue();
        assertThat(attackingRhino.getAttackTarget()).isEqualTo(ghired.getAttackTarget());
    }

    @Test
    @DisplayName("Does not populate when it attacks without a creature token")
    void doesNothingWithoutCreatureToken() {
        addCreatureReady(player1, new GhiredConclaveExile());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino")).isEmpty();
    }

    private void addGhiredMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private static Card rhinoToken() {
        Card card = new Card();
        card.setName("Rhino");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setSubtypes(List.of(CardSubtype.RHINO));
        card.setKeywords(java.util.Set.of(Keyword.TRAMPLE));
        card.setPower(4);
        card.setToughness(4);
        card.setToken(true);
        return card;
    }
}
