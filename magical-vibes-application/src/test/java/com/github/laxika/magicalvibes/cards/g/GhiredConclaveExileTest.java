package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.cards.a.AmuletOfVigor;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.v.VraskaTheUnseen;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhiredConclaveExile.class, AmuletOfVigor.class, SolRing.class, VraskaTheUnseen.class})
class GhiredConclaveExileTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 4/4 green Rhino creature token with trample")
    void entersWithRhinoToken() {
        harness.castFromHand(player1, new GhiredConclaveExile(), "{2}{R}{G}{W}");
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

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
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

    @Test
    @DisplayName("The populated token may attack a different defender from Ghired")
    void choosesDefenderIndependently() {
        addCreatureReady(player1, new GhiredConclaveExile());
        harness.addToBattlefield(player1, rhinoToken());
        Permanent vraska = harness.enterBattlefieldAndReturn(player2, new VraskaTheUnseen());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handlePermanentChosen(player1, vraska.getId());
            resolveAllTriggers();
        });

        Permanent copy = findPermanents(player1, "Rhino").getLast();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(vraska.getId());
        assertThat(findPermanent(player1, "Ghired, Conclave Exile").getAttackTarget())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Populate chooses among creature tokens you control without copying their counters or tapped state")
    void choosesTokenWithoutCopyingCounters() {
        addCreatureReady(player1, new GhiredConclaveExile());
        Permanent first = harness.addToBattlefieldAndReturn(player1, rhinoToken());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, rhinoToken());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        chosen.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handlePermanentChosen(player1, chosen.getId());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Rhino")).hasSize(3);
        Permanent copy = findPermanents(player1, "Rhino").getLast();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.getEffectivePower()).isEqualTo(4);
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(first.isAttacking()).isFalse();
        assertThat(chosen.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Populate cannot copy an opponent's creature token")
    void ignoresOpponentsTokens() {
        addCreatureReady(player1, new GhiredConclaveExile());
        harness.addToBattlefield(player2, rhinoToken());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Rhino")).isEmpty();
        assertThat(findPermanents(player2, "Rhino")).hasSize(1);
    }

    @Test
    @DisplayName("A populated copy of an animated noncreature token enters tapped without attacking")
    void noncreatureCopyStillEntersTapped() {
        addCreatureReady(player1, new GhiredConclaveExile());
        SolRing tokenCard = new SolRing();
        tokenCard.setToken(true);
        Permanent animatedToken = harness.addToBattlefieldAndReturn(player1, tokenCard);
        animatedToken.setAnimatedUntilEndOfTurn(true);
        animatedToken.setAnimatedPower(1);
        animatedToken.setAnimatedToughness(1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2);
        Permanent copy = findPermanents(player1, "Sol Ring").getLast();
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The populated token enters tapped and triggers Amulet of Vigor")
    void tappedEntryTriggersAmuletOfVigor() {
        addCreatureReady(player1, new GhiredConclaveExile());
        harness.addToBattlefield(player1, rhinoToken());
        harness.addToBattlefield(player1, new AmuletOfVigor());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Rhino")).hasSize(2);
        Permanent copy = findPermanents(player1, "Rhino").getLast();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.isTapped()).isFalse();
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
