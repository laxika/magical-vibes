package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LucentLiminid;
import com.github.laxika.magicalvibes.cards.n.Narcomoeba;
import com.github.laxika.magicalvibes.cards.s.ShimianSpecter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fleshwrither.class, LucentLiminid.class, Narcomoeba.class, ShimianSpecter.class})
class FleshwritherTest extends BaseCardTest {

    @Test
    void transfigureSacrificesSourceAndFindsCreatureWithSameManaValue() {
        Permanent source = addFleshwrither();
        Card matchingCreature = new ShimianSpecter();
        harness.setLibrary(player1, List.of(matchingCreature));
        addTransfigureMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(source.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(source.getCard().getId()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCreature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(matchingCreature.getId()));
    }

    @Test
    void transfigureSearchRequiresExactManaValue() {
        addFleshwrither();
        Card lowerManaValue = new Narcomoeba();
        Card matchingCreature = new ShimianSpecter();
        Card higherManaValue = new LucentLiminid();
        harness.setLibrary(player1, List.of(lowerManaValue, matchingCreature, higherManaValue));
        addTransfigureMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCreature);
    }

    @Test
    void transfigureCanFailToFindWhenNoCreatureHasSameManaValue() {
        Permanent source = addFleshwrither();
        Card nonMatchingCreature = new Narcomoeba();
        harness.setLibrary(player1, List.of(nonMatchingCreature));
        addTransfigureMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(nonMatchingCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(source.getCard().getId()));
    }

    @Test
    void transfigureCanOnlyBeActivatedAtSorcerySpeed() {
        addFleshwrither();
        addTransfigureMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addFleshwrither() {
        return harness.addToBattlefieldAndReturn(player1, new Fleshwrither());
    }

    private void addTransfigureMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
