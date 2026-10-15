package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.h.Harrow;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoThePit.class, GrizzlyBears.class, Opt.class, EvolvingWilds.class, Harrow.class})
class IntoThePitTest extends BaseCardTest {

    @Test
    void castsTopSpellBySacrificingANonlandPermanent() {
        harness.addToBattlefield(player1, new IntoThePit());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTopWithAdditionalCost(player1, fodder.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(opt, fodder.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(opt);
    }

    @Test
    void maySacrificeIntoThePitItselfToCastFromTheTop() {
        Permanent intoThePit = harness.addToBattlefieldAndReturn(player1, new IntoThePit());
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTopWithAdditionalCost(player1, intoThePit.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(opt, intoThePit.getCard());
    }

    @Test
    void requiresTheSacrificeSelectionBeforeRemovingTheTopCard() {
        harness.addToBattlefield(player1, new IntoThePit());
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opt);
    }

    @Test
    void topCardIsVisibleOnlyToItsControllerEvenWhenItIsALand() {
        harness.addToBattlefield(player1, new IntoThePit());
        harness.setLibrary(player1, List.of(new EvolvingWilds()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Evolving Wilds"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void doesNotAllowPlayingLandsFromTheTop() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new IntoThePit());
        EvolvingWilds land = new EvolvingWilds();
        harness.setLibrary(player1, List.of(land));

        assertThatThrownBy(() -> harness.castFromLibraryTopWithAdditionalCost(player1, pit.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertOnBattlefield(player1, "Into the Pit");
    }

    @Test
    void cannotSacrificeALandToCastASpell() {
        harness.addToBattlefield(player1, new IntoThePit());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        IntoThePit spell = new IntoThePit();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTopWithAdditionalCost(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
        harness.assertOnBattlefield(player1, "Evolving Wilds");
    }

    @Test
    void cannotSacrificeAnOpponentsNonlandPermanent() {
        harness.addToBattlefield(player1, new IntoThePit());
        Permanent opponentPit = harness.addToBattlefieldAndReturn(player2, new IntoThePit());
        IntoThePit spell = new IntoThePit();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTopWithAdditionalCost(player1, opponentPit.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
        harness.assertOnBattlefield(player2, "Into the Pit");
    }

    @Test
    void sacrificeDoesNotReplaceTheSpellsManaCost() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new IntoThePit());
        IntoThePit spell = new IntoThePit();
        harness.setLibrary(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFromLibraryTopWithAdditionalCost(player1, pit.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
        harness.assertOnBattlefield(player1, "Into the Pit");
    }

    @Test
    void enchantmentFromTheTopStillRequiresSorceryTiming() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new IntoThePit());
        IntoThePit spell = new IntoThePit();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTopWithAdditionalCost(player1, pit.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
        harness.assertOnBattlefield(player1, "Into the Pit");
    }

    @Test
    void multipleCopiesRequireOnlyOneSacrificeForTheChosenPermission() {
        Permanent firstPit = harness.addToBattlefieldAndReturn(player1, new IntoThePit());
        harness.addToBattlefield(player1, new IntoThePit());
        IntoThePit spell = new IntoThePit();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveFromLibraryTopWithAdditionalCost(player1, firstPit.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstPit.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void castsSpellsWithTheirOwnAdditionalCostsByPayingBothSacrifices() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new IntoThePit());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        Harrow harrow = new Harrow();
        harness.setLibrary(player1, List.of(harrow));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.ensurePriority(player1);

        gs.playCardFromLibraryTop(gd, player1, null, null, List.of(),
                List.of(pit.getId(), land.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pit.getCard(), land.getCard());
    }
}
