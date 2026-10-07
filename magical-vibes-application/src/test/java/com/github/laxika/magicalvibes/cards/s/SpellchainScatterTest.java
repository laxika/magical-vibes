package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellchainScatter.class, Shock.class, LavaAxe.class, SliverConstruct.class})
class SpellchainScatterTest extends BaseCardTest {

    @Test
    void unKickedSpellConjuresDuplicateAndDiscardsItAtNextEndStep() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Shock") && card.isTokenCard())
                .findFirst().orElseThrow();
        assertThat(duplicate.isTokenCard()).isTrue();

        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(duplicate);

        advanceToNextEndStep();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(duplicate);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicate);
    }

    @Test
    void kickedSpellKeepsTheConjuredDuplicate() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Shock") && card.isTokenCard())
                .findFirst().orElseThrow();
        resolveAllTriggers();
        advanceToNextEndStep();

        assertThat(gd.playerHands.get(player1.getId())).contains(duplicate);
    }

    @Test
    void unkickedDuplicateWaitsForItsControllersEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new SpellchainScatter(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(duplicate);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(duplicate);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicate);
    }

    @Test
    void nextSorceryConjuresADuplicateBeforeTheSorceryResolves() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveInstant(player1, 0);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Lava Axe");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertLife(player2, 15);
    }

    @Test
    void onlyTheNextMatchingSpellIsDuplicated() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        Card duplicate = gd.playerHands.get(player1.getId()).get(1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(duplicate);
        harness.assertLife(player2, 16);
    }

    @Test
    void creatureSpellDoesNotConsumeTheDelayedTrigger() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new SliverConstruct(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Sliver Construct");

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsSpellDoesNotConsumeTheDelayedTrigger() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void unusedTriggerExpiresWhenTheTurnEnds() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);
        advanceToNextEndStep();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    void conjuredDuplicateCanBeCastBeforeTheDiscardTrigger() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicate);

        advanceToNextEndStep();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicate);
    }

    private void advanceToNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
