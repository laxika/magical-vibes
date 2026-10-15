package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cloudpost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Trenchpost.class, Cloudpost.class})
class TrenchpostTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent trenchpost = harness.addToBattlefieldAndReturn(player1, new Trenchpost());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(trenchpost.isTapped()).isTrue();
    }

    @Test
    void millsOneCardForEachLocusControlled() {
        harness.addToBattlefield(player1, new Trenchpost());
        harness.addToBattlefield(player1, new Cloudpost());
        harness.addToBattlefield(player1, new Cloudpost());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player2, List.of(
                new Trenchpost(), new Trenchpost(), new Trenchpost(),
                new Trenchpost(), new Trenchpost()));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void canMillItsControllerAndIgnoresOpposingLoci() {
        Permanent trenchpost = harness.addToBattlefieldAndReturn(player1, new Trenchpost());
        harness.addToBattlefield(player1, new Cloudpost());
        harness.addToBattlefield(player2, new Cloudpost());
        harness.addToBattlefield(player2, new Trenchpost());
        Trenchpost first = new Trenchpost();
        Cloudpost second = new Cloudpost();
        Trenchpost third = new Trenchpost();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 1, null, player1.getId());

        assertThat(trenchpost.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void countsLociAddedBeforeResolution() {
        harness.addToBattlefield(player1, new Trenchpost());
        harness.setLibrary(player2, List.of(new Trenchpost(), new Trenchpost(), new Trenchpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.addToBattlefield(player1, new Cloudpost());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void sourceLeavingDoesNotStopAbilityOrCountTheMissingSource() {
        Permanent trenchpost = harness.addToBattlefieldAndReturn(player1, new Trenchpost());
        harness.addToBattlefield(player1, new Cloudpost());
        harness.setLibrary(player2, List.of(new Trenchpost(), new Trenchpost(), new Trenchpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(trenchpost);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    void millsNothingIfNoLociRemainAtResolution() {
        Permanent trenchpost = harness.addToBattlefieldAndReturn(player1, new Trenchpost());
        Trenchpost libraryCard = new Trenchpost();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(trenchpost);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void millsOnlyAvailableCardsFromShortLibrary() {
        harness.addToBattlefield(player1, new Trenchpost());
        harness.addToBattlefield(player1, new Cloudpost());
        Trenchpost libraryCard = new Trenchpost();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(libraryCard);
    }

    @Test
    void requiresThreeMana() {
        Permanent trenchpost = harness.addToBattlefieldAndReturn(player1, new Trenchpost());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(trenchpost.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotMillWithTappedTrenchpost() {
        harness.addToBattlefield(player1, new Trenchpost());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
