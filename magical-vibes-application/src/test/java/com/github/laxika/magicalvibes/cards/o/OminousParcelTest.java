package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OminousParcel.class, CrawWurm.class, Forest.class, GrizzlyBears.class, Island.class})
class OminousParcelTest extends BaseCardTest {

    @Test
    @DisplayName("The land ability searches for a basic land and sacrifices Ominous Parcel")
    void searchesForBasicLand() {
        addParcelAndMana(2);
        Card forest = new Forest();
        Card island = new Island();
        setLibrary(forest, island, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, island);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        harness.assertInGraveyard(player1, "Ominous Parcel");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The damage ability deals 4 damage to a creature and sacrifices Ominous Parcel")
    void dealsDamageToCreature() {
        addParcelAndMana(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Ominous Parcel");
    }

    @Test
    @DisplayName("The damage ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addParcelAndMana(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addParcelAndMana(int mana) {
        harness.addToBattlefield(player1, new OminousParcel());
        harness.addMana(player1, ManaColor.COLORLESS, mana);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
