package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SymbioteSpiderMan.class, Forest.class, GrizzlyBears.class})
class SymbioteSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage looks at that many cards, putting one into hand and the rest into the graveyard")
    void combatDamageLooksAtThatManyCards() {
        Permanent spiderMan = addAttackingSpiderMan();
        Forest handCard = new Forest();
        Forest graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(handCard, graveyardCard));

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(handCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(spiderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Find New Host exiles Symbiote Spider-Man, adds a counter, and grants its combat-damage ability")
    void findNewHostAddsCounterAndGrantsAbility() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Forest handCard = new Forest();
        Forest graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(handCard, graveyardCard));
        harness.setGraveyard(player1, List.of(new SymbioteSpiderMan()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.assertNotInGraveyard(player1, "Symbiote Spider-Man");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        target.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    @DisplayName("Find New Host can only target a creature you control and can only be activated as a sorcery")
    void findNewHostValidatesTargetAndTiming() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SymbioteSpiderMan()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttackingSpiderMan() {
        return addAttackingSpiderMan(player1);
    }

    private Permanent addAttackingSpiderMan(Player player) {
        Permanent spiderMan = addCreatureReady(player, new SymbioteSpiderMan());
        spiderMan.setAttacking(true);
        return spiderMan;
    }
}
