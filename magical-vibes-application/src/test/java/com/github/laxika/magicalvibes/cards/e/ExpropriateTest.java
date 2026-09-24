package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Expropriate.class, GrizzlyBears.class})
class ExpropriateTest extends BaseCardTest {

    @Test
    @DisplayName("Each time vote grants an extra turn and the spell is exiled")
    void timeVotesGrantExtraTurns() {
        Expropriate spell = cast();

        harness.handleListChoice(player1, ChoiceContext.ExpropriateChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.ExpropriateChoice.TIME);

        assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Money votes let the caster choose permanents owned by each voter")
    void moneyVotesStealOwnedPermanents() {
        Permanent ownPermanent = addOwnedPermanent(player1, new GrizzlyBears());
        Permanent ownAlternative = addOwnedPermanent(player1, new GrizzlyBears());
        Permanent opponentPermanent = addOwnedPermanent(player2, new GrizzlyBears());
        Permanent opponentAlternative = addOwnedPermanent(player2, new GrizzlyBears());
        Expropriate spell = cast();

        harness.handleListChoice(player1, ChoiceContext.ExpropriateChoice.MONEY);
        harness.handleListChoice(player2, ChoiceContext.ExpropriateChoice.MONEY);

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactlyInAnyOrder(ownPermanent.getId(), ownAlternative.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownPermanent.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player1.getId());
        assertThat(secondChoice.validIds()).containsExactlyInAnyOrder(opponentPermanent.getId(), opponentAlternative.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(opponentPermanent.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownPermanent, opponentPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentPermanent);
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private Expropriate cast() {
        Expropriate spell = new Expropriate();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(activeVote()).isNotNull();
        return spell;
    }

    private Permanent addOwnedPermanent(Player owner, Card card) {
        card.setOwnerId(owner.getId());
        return harness.addToBattlefieldAndReturn(owner, card);
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.ExpropriateChoice.OPTIONS);
        return choice;
    }
}
