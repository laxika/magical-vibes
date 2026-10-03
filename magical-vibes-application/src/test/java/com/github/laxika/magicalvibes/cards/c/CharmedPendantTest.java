package com.github.laxika.magicalvibes.cards.c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.a.AbandonedOutpost;
import com.github.laxika.magicalvibes.cards.a.Atogatog;
import com.github.laxika.magicalvibes.cards.d.Dismember;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({
        AbandonedOutpost.class, Atogatog.class, CharmedPendant.class,
        Dismember.class, LeylineOfTheVoid.class, SelesnyaGuildmage.class
})
class CharmedPendantTest extends BaseCardTest {

    @Test
    @DisplayName("Mills a card and adds mana for each colored symbol in its mana cost")
    void millsAndAddsManaForEachColoredSymbol() {
        harness.forceActivePlayer(player1);
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new CharmedPendant());
        Card milled = new Atogatog();
        harness.setLibrary(player1, List.of(milled));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pendant.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not add mana for a card with no colored mana symbols")
    void doesNotAddManaForCardWithNoColoredManaSymbols() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefieldAndReturn(player1, new CharmedPendant());
        Card milled = new AbandonedOutpost();
        harness.setLibrary(player1, List.of(milled));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Chooses one color for each hybrid mana symbol")
    void choosesOneColorForEachHybridManaSymbol() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefieldAndReturn(player1, new CharmedPendant());
        Card milled = new SelesnyaGuildmage();
        harness.setLibrary(player1, List.of(milled));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.get(ManaColor.BLACK)).isZero();
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Counts the colored half of each Phyrexian mana symbol")
    void countsColorsInPhyrexianManaSymbols() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefieldAndReturn(player1, new CharmedPendant());
        Card milled = new Dismember();
        harness.setLibrary(player1, List.of(milled));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(pool.get(ManaColor.WHITE)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Cannot be activated when the library is empty")
    void cannotActivateWithEmptyLibrary() {
        harness.forceActivePlayer(player1);
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new CharmedPendant());
        harness.setLibrary(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to mill");

        assertThat(pendant.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mills as an activation cost but adds mana only on resolution")
    void millsBeforeResolutionAndAddsManaOnResolution() {
        harness.forceActivePlayer(player2);
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new CharmedPendant());
        Card milled = new Atogatog();
        harness.setLibrary(player1, List.of(milled));

        harness.activateAbility(player1, 0, null, null);

        assertThat(pendant.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each pending activation remembers its own milled card")
    void pendingActivationsRememberTheirOwnMilledCards() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new CharmedPendant());
        harness.addToBattlefield(player1, new CharmedPendant());
        Card first = new Atogatog();
        Card second = new AbandonedOutpost();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            assertThat(pool.get(color)).isEqualTo(1);
        }
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Uses the milled card's mana cost when it is exiled instead of entering the graveyard")
    void addsManaWhenMilledCardIsExiledInstead() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new CharmedPendant());
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        Card milled = new Atogatog();
        harness.setLibrary(player1, List.of(milled));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(milled.getId())).isNotNull();
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            assertThat(pool.get(color)).isEqualTo(1);
        }
    }
}
