package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmenOfTheHunt.class, Plains.class, Forest.class, Island.class, GrizzlyBears.class})
class OmenOfTheHuntTest extends BaseCardTest {

    @Test
    void enteringBattlefieldMayPutBasicLandOntoBattlefieldTapped() {
        Card plains = new Plains();
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(plains, forest, island, new GrizzlyBears()));
        harness.setHand(player1, List.of(new OmenOfTheHunt()));
        addOmenMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains, forest, island);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
    }

    @Test
    void decliningEnterTheBattlefieldAbilityDoesNotSearch() {
        harness.setHand(player1, List.of(new OmenOfTheHunt()));
        addOmenMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Omen of the Hunt");
    }

    @Test
    void sacrificingOmenScriesTwo() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheHunt());
        addOmenMana();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(omen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(omen.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(firstCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, firstCard);
    }

    private void addOmenMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
