package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LonisCryptozoologist.class, GrizzlyBears.class, HillGiant.class})
class LonisCryptozoologistTest extends BaseCardTest {

    @Test
    void investigatesWhenAnotherNontokenCreatureEnters() {
        harness.addToBattlefield(player1, new LonisCryptozoologist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void sacrificesCluesToTakeAnEligiblePermanentFromTargetLibrary() {
        Permanent lonis = harness.addToBattlefieldAndReturn(player1, new LonisCryptozoologist());
        lonis.setSummoningSick(false);
        Permanent sacrificedOne = addClueToken();
        Permanent sacrificedTwo = addClueToken();
        GrizzlyBears eligible = new GrizzlyBears();
        HillGiant tooExpensive = new HillGiant();
        harness.setLibrary(player2, List.of(eligible, tooExpensive));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lonis),
                0, 2, player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(sacrificedOne, sacrificedTwo);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(eligible);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(tooExpensive);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private Permanent addClueToken() {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        Permanent clue = new Permanent(clueCard);
        clue.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(clue);
        return clue;
    }
}
