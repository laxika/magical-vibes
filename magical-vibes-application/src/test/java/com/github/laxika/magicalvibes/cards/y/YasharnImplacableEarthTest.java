package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Greed;
import com.github.laxika.magicalvibes.cards.h.Harrow;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YasharnImplacableEarth.class, Forest.class, Plains.class, Atog.class, Spellbook.class, Greed.class, Harrow.class})
class YasharnImplacableEarthTest extends BaseCardTest {

    @Test
    void entersAndSearchesForAForestAndAPlains() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        Card other = new Spellbook();
        harness.setLibrary(player1, List.of(forest, plains, other));
        harness.setHand(player1, List.of(new YasharnImplacableEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
    }

    @Test
    void preventsLifePaymentsAndNonlandPermanentSacrificesAsCosts() {
        harness.addToBattlefield(player1, new YasharnImplacableEarth());
        harness.addToBattlefield(player1, new Greed());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pay life");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.addToBattlefield(player1, new Atog());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifact.getId()));
    }

    @Test
    void stillAllowsSacrificingALandAsACost() {
        harness.addToBattlefield(player1, new YasharnImplacableEarth());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Harrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithSacrifice(player1, 0, null, land.getId());

        harness.assertInGraveyard(player1, "Forest");
    }
}
