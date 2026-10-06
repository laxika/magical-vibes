package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BelbesPortal;
import com.github.laxika.magicalvibes.cards.s.SkyshroudRidgeback;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({RathiAssassin.class, SkyshroudRidgeback.class, RathiIntimidator.class,
        RathiFiend.class, BelbesPortal.class})
class RathiAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("First ability destroys a tapped nonblack creature")
    void destroysTappedNonblackCreature() {
        Permanent assassin = addRathiAssassin();
        Permanent ridgeback = addCreatureReady(player2, new SkyshroudRidgeback());
        ridgeback.tap();
        addBlackManaCost();

        harness.activateAbility(player1, 0, null, ridgeback.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ridgeback);
        assertThat(assassin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First ability rejects untapped and black creatures")
    void firstAbilityRejectsIllegalTargets() {
        addRathiAssassin();
        Permanent ridgeback = addCreatureReady(player2, new SkyshroudRidgeback());
        addBlackManaCost();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ridgeback.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped nonblack creature");

        Permanent intimidator = addCreatureReady(player2, new RathiIntimidator());
        intimidator.tap();
        addBlackManaCost();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, intimidator.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped nonblack creature");
    }

    @Test
    @DisplayName("First ability rejects tapped noncreatures")
    void firstAbilityRejectsTappedNoncreature() {
        addRathiAssassin();
        Permanent portal = harness.addToBattlefieldAndReturn(player2, new BelbesPortal());
        portal.tap();
        addBlackManaCost();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, portal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped nonblack creature");
    }

    @Test
    @DisplayName("Second ability puts a qualifying Mercenary permanent onto the battlefield")
    void searchesMercenaryPermanentWithManaValueAtMostThree() {
        Permanent assassin = addRathiAssassin();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(
                new RathiIntimidator(), new SkyshroudRidgeback(), new RathiFiend()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(assassin.isTapped()).isTrue();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Rathi Intimidator");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Rathi Intimidator");
        harness.assertNotOnBattlefield(player1, "Skyshroud Ridgeback");
        harness.assertNotOnBattlefield(player1, "Rathi Fiend");
    }

    @Test
    @DisplayName("Destruction fails if the target untaps before resolution")
    void doesNotDestroyTargetThatUntaps() {
        addRathiAssassin();
        Permanent ridgeback = addCreatureReady(player2, new SkyshroudRidgeback());
        ridgeback.tap();
        addBlackManaCost();

        harness.activateAbility(player1, 0, null, ridgeback.getId());
        ridgeback.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ridgeback);
    }

    @Test
    @DisplayName("A restricted Mercenary search may fail to find an eligible card")
    void mayDeclineToFindMercenary() {
        addRathiAssassin();
        RathiIntimidator intimidator = new RathiIntimidator();
        harness.setLibrary(player1, List.of(intimidator));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Rathi Intimidator");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(intimidator);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Mercenary search with no eligible cards completes without a choice")
    void searchWithoutEligibleCardsCompletes() {
        addRathiAssassin();
        harness.setLibrary(player1, List.of(new RathiFiend(), new SkyshroudRidgeback()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rathi Fiend");
        harness.assertNotOnBattlefield(player1, "Skyshroud Ridgeback");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addRathiAssassin() {
        return addCreatureReady(player1, new RathiAssassin());
    }

    private void addBlackManaCost() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
