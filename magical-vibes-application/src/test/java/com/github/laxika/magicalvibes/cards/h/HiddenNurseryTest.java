package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BaskingCapybara;
import com.github.laxika.magicalvibes.cards.b.BitterTriumph;
import com.github.laxika.magicalvibes.cards.c.CavernStomper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenNursery.class, Forest.class, GrizzlyBears.class, BaskingCapybara.class,
        CavernStomper.class, BitterTriumph.class, QuintoriusKand.class})
class HiddenNurseryTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new HiddenNursery()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void tapsForGreenMana() {
        Permanent nursery = addReadyNursery();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(nursery.isTapped()).isTrue();
    }

    @Test
    void sacrificesAndDiscoversFour() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), discovered));
        Permanent nursery = addReadyNursery();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nursery.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    private Permanent addReadyNursery() {
        return addCreatureReady(player1, new HiddenNursery());
    }

    @Test
    void castsDiscoveredCreatureWithoutManaAndBottomsSkippedCards() {
        Forest skippedLand = new Forest();
        CavernStomper skippedCreature = new CavernStomper();
        BaskingCapybara discovered = new BaskingCapybara();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, skippedCreature, discovered, untouched));
        Permanent nursery = addReadyNursery();
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nursery);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nursery.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(discovered));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(untouched, skippedLand, skippedCreature);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Basking Capybara");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
    }

    @Test
    void returnsEntireLibraryWhenNoCardQualifies() {
        Forest land = new Forest();
        CavernStomper expensive = new CavernStomper();
        harness.setLibrary(player1, List.of(land, expensive));
        addReadyNursery();
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land, expensive);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent nursery = addReadyNursery();
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nursery.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingDiscoveredSpellStillPaysItsMandatoryAdditionalCost() {
        BitterTriumph discovered = new BitterTriumph();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new BaskingCapybara());
        addReadyNursery();
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(discovered));
        harness.assertLife(player1, 17);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Basking Capybara");
    }

    @Test
    void cannotDiscoverOutsideMainPhase() {
        Permanent nursery = addReadyNursery();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nursery);
        assertThat(nursery.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    void discoveredSpellTriggersCastingFromExileAbility() {
        harness.setLibrary(player1, List.of(new BaskingCapybara()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyNursery();
        harness.addToBattlefield(player1, new QuintoriusKand());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Basking Capybara");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
