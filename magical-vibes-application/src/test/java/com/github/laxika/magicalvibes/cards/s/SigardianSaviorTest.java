package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Aethersnatch;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigardianSavior.class, GrizzlyBears.class, HillGiant.class, Zombify.class, Spellbook.class, Aethersnatch.class})
class SigardianSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, returns up to two small creature cards from the graveyard")
    void returnsUpToTwoSmallCreaturesWhenCast() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(first, second, tooExpensive));
        castSigardianSavior();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(tooExpensive.getId());
    }

    @Test
    @DisplayName("Does not return creatures when it enters without being cast")
    void doesNotReturnCreaturesWhenNotCast() {
        Card returnedCard = new GrizzlyBears();
        Card savior = new SigardianSavior();
        Card zombify = new Zombify();
        harness.setGraveyard(player1, List.of(savior, returnedCard));
        harness.setHand(player1, List.of(zombify));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, savior.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(savior.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(returnedCard.getId(), zombify.getId());
    }

    @Test
    void canChooseZeroTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castSigardianSavior();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sigardian Savior");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseOnlyOneOfTwoEligibleCreatures() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        castSigardianSavior();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId()).doesNotContain(second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
    }

    @Test
    void excludesNoncreaturesAndOpponentsGraveyard() {
        Card eligible = new GrizzlyBears();
        Card noncreature = new Spellbook();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible, noncreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        castSigardianSavior();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void resolvesWithoutEligibleTargets() {
        Card expensive = new HillGiant();
        harness.setGraveyard(player1, List.of(expensive));
        castSigardianSavior();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sigardian Savior");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(expensive);
    }

    @Test
    void returnsRemainingTargetWhenOtherTargetLeavesGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        castSigardianSavior();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        // Remove one selected card before the triggered ability resolves.
        harness.setGraveyard(player1, List.of(second));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(second.getId()).doesNotContain(first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerForPlayerWhoStoleTheCreatureSpell() {
        Card savior = new SigardianSavior();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(savior));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, savior.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sigardian Savior");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
    }

    private void castSigardianSavior() {
        harness.setHand(player1, List.of(new SigardianSavior()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
