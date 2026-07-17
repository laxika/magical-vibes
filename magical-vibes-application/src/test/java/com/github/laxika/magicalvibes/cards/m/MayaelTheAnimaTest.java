package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MayaelTheAnimaTest extends BaseCardTest {

    @Test
    @DisplayName("Ability offers only creature cards with power 5 or greater")
    void offersOnlyHighPowerCreatures() {
        setupMayaelAndActivate(List.of(
                new AvatarOfMight(), // 8/8 — qualifies
                new HillGiant(),     // 3/3 creature — power too low
                new GrizzlyBears(),  // 2/2 creature — power too low
                new Shock(),         // instant — not a creature
                new Plains()         // land — not a creature
        ));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1);
        assertThat(offered.getFirst().getName()).isEqualTo("Avatar of Might");
    }

    @Test
    @DisplayName("Choosing a qualifying creature puts it onto the battlefield and reorders the rest")
    void choosingPutsCreatureOnBattlefield() {
        setupMayaelAndActivate(List.of(
                new AvatarOfMight(), new LlanowarElves(), new Shock(), new Plains(), new Plains()
        ));

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Avatar of Might"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("May decline to put a creature onto the battlefield")
    void mayDeclineToPutCreature() {
        setupMayaelAndActivate(List.of(
                new AvatarOfMight(), new LlanowarElves(), new Shock(), new Plains(), new Plains()
        ));

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("No qualifying creatures sends all five cards to the bottom")
    void noQualifyingCreaturesReordersAll() {
        setupMayaelAndActivate(List.of(
                new HillGiant(), new GrizzlyBears(), new LlanowarElves(), new Shock(), new Plains()
        ));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    /**
     * Puts a ready Mayael on player1's battlefield, stacks the given cards on top of the library,
     * then pays for and resolves the {@code {3}{R}{G}{W}, {T}} ability.
     */
    private void setupMayaelAndActivate(List<Card> topCards) {
        addCreatureReady(player1, new MayaelTheAnima());

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(topCards);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
