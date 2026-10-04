package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HurkylsRecall;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamesOfMoradin.class, SolRing.class, GrizzlyBears.class, HurkylsRecall.class})
class FlamesOfMoradinTest extends BaseCardTest {

    @Test
    void destroysArtifactsAndConjuresModifiedNontokenCopies() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Card tokenCard = new SolRing();
        tokenCard.setToken(true);
        Permanent tokenArtifact = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.setHand(player1, List.of(new FlamesOfMoradin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(artifact.getId(), tokenArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Sol Ring", "Flames of Moradin")
                .hasSize(2);
        List<Card> copies = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Sol Ring"))
                .toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getId()).isNotEqualTo(artifact.getCard().getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithAlternateCost(player1,
                gd.playerHands.get(player1.getId()).indexOf(copies.getFirst()), List.of());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getName)
                .containsExactly("Sol Ring");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void onlyArtifactsCanBeTargeted() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlamesOfMoradin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void canResolveWithoutTargets() {
        harness.setHand(player1, List.of(new FlamesOfMoradin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Flames of Moradin");
    }

    @Test
    void destroysThreeArtifactsAndConjuresOpponentsArtifactsIntoCastersHand() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent firstOpponent = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent secondOpponent = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new FlamesOfMoradin()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0,
                List.of(own.getId(), firstOpponent.getId(), secondOpponent.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3)
                .allSatisfy(card -> assertThat(card.getOwnerId()).isEqualTo(player1.getId()));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void cannotChooseFourArtifacts() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new FlamesOfMoradin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotConjureDuplicateOfRegeneratedArtifact() {
        Permanent regenerated = harness.addToBattlefieldAndReturn(player2, new SolRing());
        regenerated.setRegenerationShield(1);
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new FlamesOfMoradin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(regenerated.getId(), destroyed.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(regenerated);
        assertThat(regenerated.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void perpetualAbilitiesSurviveReturningDuplicateToHand() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new FlamesOfMoradin(), new HurkylsRecall()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(original.getId()));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithAlternateCost(player1, 1, List.of());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sol Ring");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sol Ring");
    }

    @Test
    void duplicateCanBeHeldForLaterTurnAndCastForNormalManaCost() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new FlamesOfMoradin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(original.getId()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sol Ring");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
