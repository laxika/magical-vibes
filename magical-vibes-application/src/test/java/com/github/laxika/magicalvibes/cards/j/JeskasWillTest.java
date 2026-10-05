package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeskasWill.class, GrizzlyBears.class, EdgarMarkov.class, Forest.class, Shock.class})
class JeskasWillTest extends BaseCardTest {

    @Test
    void addsRedManaForEachCardInTargetOpponentsHand() {
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.setHand(player2, List.of(new Shock(), new Forest(), new GrizzlyBears()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    void exilesTopThreeCardsWithPlayPermission() {
        Card first = new Shock();
        Card second = new Forest();
        Card third = new GrizzlyBears();
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.setLibrary(player1, List.of(first, second, third));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId(), third.getId());
    }

    @Test
    void commanderAllowsBothModes() {
        Card commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        harness.addToBattlefield(player1, commander);
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.setHand(player2, List.of(new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(new Shock(), new Forest(), new GrizzlyBears()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    void cannotChooseBothModesWithoutACommander() {
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.setHand(player2, List.of(new Shock()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(player2.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manaModeRequiresAnOpponentTarget() {
        harness.setHand(player1, List.of(new JeskasWill()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(player1.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesOpponentsHandSizeAtResolution() {
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.setHand(player2, List.of(new Shock(), new Forest(), new GrizzlyBears()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(player2.getId()), null);
        harness.setHand(player2, List.of(new Forest()));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void emptyOpponentHandAddsNoMana() {
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void exilesAllAvailableCardsFromAShortLibrary() {
        Card card = new Forest();
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.setLibrary(player1, List.of(card));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
    }

    @Test
    void commanderInCommandZoneDoesNotAllowBothModes() {
        Card commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        gd.playerCommandZones.get(player1.getId()).add(commander);
        harness.setHand(player1, List.of(new JeskasWill()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(player2.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesStillResolveAfterCommanderLeavesBattlefield() {
        Card commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        harness.addToBattlefield(player1, commander);
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.setHand(player2, List.of(new Forest()));
        Card exiled = new Forest();
        harness.setLibrary(player1, List.of(exiled));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId()), null);
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerCommandZones.get(player1.getId()).add(commander);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
