package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatheringStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Gathering Stone enters stores that type")
    void choosesCreatureTypeOnEntry() {
        gd.playerDecks.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new GatheringStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Gathering Stone").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("The entering ability can reveal a matching top card to hand")
    void matchingTopCardOnEntryGoesToHand() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.setHand(player1, List.of(new GatheringStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("Spells of the chosen type cost {1} less to cast")
    void reducesChosenTypeSpellCost() {
        gd.playerBattlefields.get(player1.getId()).add(gatheringStone(CardSubtype.BEAR));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Spells without the chosen type are not reduced")
    void doesNotReduceOtherTypeSpellCost() {
        gd.playerBattlefields.get(player1.getId()).add(gatheringStone(CardSubtype.BEAR));
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The upkeep ability may reveal a matching top card to hand")
    void matchingTopCardGoesToHand() {
        Permanent stone = gatheringStone(CardSubtype.BEAR);
        gd.playerBattlefields.get(player1.getId()).add(stone);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("A nonmatching top card stays on top when the graveyard option is declined")
    void nonmatchingTopCardStaysOnTopWhenDeclined() {
        Permanent stone = gatheringStone(CardSubtype.BEAR);
        gd.playerBattlefields.get(player1.getId()).add(stone);
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setLibrary(player1, List.of(corpse));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(corpse);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(corpse);
    }

    private Permanent gatheringStone(CardSubtype chosenSubtype) {
        Permanent permanent = new Permanent(new GatheringStone());
        permanent.setChosenSubtype(chosenSubtype);
        return permanent;
    }
}
