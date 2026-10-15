package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoreclawTerrorOfQalSisma;
import com.github.laxika.magicalvibes.cards.v.VizierOfTheMenagerie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThundermaneDragon.class, GoreclawTerrorOfQalSisma.class, GrizzlyBears.class,
        VizierOfTheMenagerie.class})
class ThundermaneDragonTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature with power 4 or greater from the top and gives it haste")
    void castsHighPowerCreatureFromLibraryTopWithHaste() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        GoreclawTerrorOfQalSisma goreclaw = new GoreclawTerrorOfQalSisma();
        harness.setLibrary(player1, List.of(goreclaw));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFromLibraryTop(player1);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Goreclaw, Terror of Qal Sisma");
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("does not allow a creature with power less than 4 from the top")
    void cannotCastLowPowerCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void hasteIsGrantedDuringCastingWithoutAnExtraStackEntry() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        ThundermaneDragon spell = new ThundermaneDragon();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromLibraryTop(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gqs.sourceHasKeyword(gd, gd.stack.getFirst(), null, Keyword.HASTE)).isTrue();
    }

    @Test
    void creatureCastThroughAnotherPermissionDoesNotGainHaste() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        harness.addToBattlefield(player1, new VizierOfTheMenagerie());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isFalse();
    }

    @Test
    void hasteExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        harness.setLibrary(player1, List.of(new GoreclawTerrorOfQalSisma()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveFromLibraryTop(player1);
        harness.passBothPriorities();
        Permanent entered = findPermanent(player1, "Goreclaw, Terror of Qal Sisma");
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isFalse();
    }

    @Test
    void creatureCastFromHandDoesNotGainHaste() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        harness.setHand(player1, List.of(new GoreclawTerrorOfQalSisma()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Goreclaw, Terror of Qal Sisma"),
                Keyword.HASTE)).isFalse();
    }

    @Test
    void lookingAtLibraryTopIsAllowedOnlyForController() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFromLibraryStillRequiresColoredMana() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        ThundermaneDragon spell = new ThundermaneDragon();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }
}
