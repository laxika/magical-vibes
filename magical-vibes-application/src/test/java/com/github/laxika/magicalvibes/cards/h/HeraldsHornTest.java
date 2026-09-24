package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldsHorn.class, GrizzlyBears.class, WalkingCorpse.class})
class HeraldsHornTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Herald's Horn enters stores that type")
    void choosesCreatureTypeOnEntry() {
        harness.setHand(player1, List.of(new HeraldsHorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Herald's Horn").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Creature spells of the chosen type cost {1} less")
    void reducesChosenCreatureTypeSpellCost() {
        addHorn(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Creature spells of another type are not reduced")
    void doesNotReduceAnotherCreatureType() {
        addHorn(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The upkeep ability may reveal a matching creature card to hand")
    void matchingTopCardGoesToHand() {
        addHorn(CardSubtype.BEAR);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("A nonmatching top card stays on top of the library")
    void nonmatchingTopCardStaysOnTop() {
        addHorn(CardSubtype.BEAR);
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setLibrary(player1, List.of(corpse));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(corpse);
    }

    private Permanent addHorn(CardSubtype chosenSubtype) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new HeraldsHorn());
        permanent.setChosenSubtype(chosenSubtype);
        return permanent;
    }
}
