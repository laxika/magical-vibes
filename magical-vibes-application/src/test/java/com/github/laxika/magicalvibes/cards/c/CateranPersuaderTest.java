package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeepwoodWolverine;
import com.github.laxika.magicalvibes.cards.m.MoltingHarpy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CateranPersuader.class, CateranBrute.class, CateranSummons.class, CacklingWitch.class,
        MoltingHarpy.class, DeepwoodWolverine.class})
class CateranPersuaderTest extends BaseCardTest {

    @Test
    void putsOneManaMercenaryOntoBattlefieldWithoutPayingItsManaCost() {
        Permanent persuader = addCreatureReady(player1, new CateranPersuader());
        MoltingHarpy harpy = new MoltingHarpy();
        MoltingHarpy otherHarpy = new MoltingHarpy();
        harness.setLibrary(player1, List.of(harpy, otherHarpy, new CateranPersuader(),
                new CateranBrute(), new CateranSummons(), new DeepwoodWolverine()));
        harness.setLibrary(player2, List.of(new MoltingHarpy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(persuader.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getId)
                .containsExactly(harpy.getId(), otherHarpy.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Molting Harpy").getCard().getId()).isEqualTo(harpy.getId());
        assertThat(findPermanent(player1, "Molting Harpy").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Molting Harpy").isSummoningSick()).isTrue();
        assertThat(countPermanents(player1, "Molting Harpy")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(harpy.getId()).contains(otherHarpy.getId()).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Molting Harpy");
        harness.assertNotInHand(player1, "Molting Harpy");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void mayFailToFindEvenWhenEligibleMercenaryExists() {
        addCreatureReady(player1, new CateranPersuader());
        MoltingHarpy harpy = new MoltingHarpy();
        harness.setLibrary(player1, List.of(harpy));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Molting Harpy");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(harpy.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        Permanent persuader = addCreatureReady(player1, new CateranPersuader());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(persuader.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent persuader = harness.addToBattlefieldAndReturn(player1, new CateranPersuader());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(persuader.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent persuader = addCreatureReady(player1, new CateranPersuader());
        persuader.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void doesNotFindMercenaryPermanentWithManaValueAboveOne() {
        Permanent persuader = addCreatureReady(player1, new CateranPersuader());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setLibrary(player1, List.of(new CateranBrute(), new CateranSummons(), new CacklingWitch()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNull();
        assertThat(persuader.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Cateran Brute");
        harness.assertNotOnBattlefield(player1, "Cateran Summons");
        harness.assertNotOnBattlefield(player1, "Cackling Witch");
    }

    @Test
    void cannotActivateWithoutGenericMana() {
        Permanent persuader = addCreatureReady(player1, new CateranPersuader());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(persuader.isTapped()).isFalse();
    }
}
