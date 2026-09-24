package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessRanksOfHYDRA.class, GrizzlyBears.class, Forest.class})
class EndlessRanksOfHYDRATest extends BaseCardTest {

    @Test
    @DisplayName("Creates one menace Villain for each opponent")
    void createsVillainForEachOpponent() {
        harness.setHand(player1, List.of(new EndlessRanksOfHYDRA()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> villains = findPermanents(player1, "Villain").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(villains).hasSize(1);
        assertThat(villains.getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(villains.getFirst().getEffectiveToughness()).isEqualTo(1);
        assertThat(villains.getFirst().hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A commander entering lets you pay to return the card from the graveyard")
    void commanderEnteringReturnsCard() {
        EndlessRanksOfHYDRA ranks = new EndlessRanksOfHYDRA();
        harness.setGraveyard(player1, List.of(ranks));

        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        harness.setHand(player1, List.of(commander));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(ranks.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(ranks.getId()));
    }

    @Test
    @DisplayName("A commander attack lets you pay to return the card from the graveyard")
    void commanderAttackingReturnsCard() {
        EndlessRanksOfHYDRA ranks = new EndlessRanksOfHYDRA();
        harness.setGraveyard(player1, List.of(ranks));

        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(ranks.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(ranks.getId()));
    }

    @Test
    @DisplayName("A noncommander attack does not trigger the graveyard ability")
    void noncommanderAttackDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new EndlessRanksOfHYDRA()));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("A noncreature commander entering also triggers the graveyard ability")
    void noncreatureCommanderEnteringTriggers() {
        EndlessRanksOfHYDRA ranks = new EndlessRanksOfHYDRA();
        harness.setGraveyard(player1, List.of(ranks));

        Card commander = new Forest();
        gd.makeCommander(player1.getId(), commander);
        harness.enterBattlefieldAndReturn(player1, commander);
        assertThat(ranks.getEffects(EffectSlot.GRAVEYARD_ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
