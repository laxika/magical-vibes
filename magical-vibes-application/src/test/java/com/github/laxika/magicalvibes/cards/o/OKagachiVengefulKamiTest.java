package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AstralSlide;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvisibleStalker;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OKagachiVengefulKami.class, AstralSlide.class, Forest.class, GrizzlyBears.class, InvisibleStalker.class})
class OKagachiVengefulKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nonland permanent when the damaged player attacked last turn")
    void exilesPermanentAfterLastTurnAttack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AstralSlide());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveCombat(player2);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        Permanent kami = addCreatureReady(player1, new OKagachiVengefulKami());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kami)));
        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(target.getId(), attacker.getId())
                .doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Astral Slide");
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card.getName().equals("Astral Slide"));
    }

    @Test
    @DisplayName("Does not trigger when the damaged player did not attack last turn")
    void doesNotTriggerWithoutLastTurnAttack() {
        Permanent kami = addCreatureReady(player1, new OKagachiVengefulKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AstralSlide());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kami)));
        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Astral Slide");
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getName().equals(target.getCard().getName()));
    }

    @Test
    @DisplayName("Cannot exile an opponent's hexproof permanent when there are no legal targets")
    void cannotExileHexproofPermanent() {
        Permanent stalker = addCreatureReady(player2, new InvisibleStalker());
        harness.addToBattlefield(player2, new Forest());
        finishOpponentAttack(stalker);

        Permanent kami = addCreatureReady(player1, new OKagachiVengefulKami());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kami)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Invisible Stalker");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An older attack does not qualify after the opponent takes a turn without attacking")
    void olderAttackDoesNotQualifyAfterNonattackingTurn() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        finishOpponentAttack(attacker);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        Permanent kami = addCreatureReady(player1, new OKagachiVengefulKami());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kami)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void finishOpponentAttack(Permanent attacker) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveCombat(player2);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }

}
