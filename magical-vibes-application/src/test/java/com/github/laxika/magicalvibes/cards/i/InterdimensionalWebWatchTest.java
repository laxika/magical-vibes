package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InterdimensionalWebWatch.class, Divination.class, Forest.class, GrizzlyBears.class})
class InterdimensionalWebWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards and lets you play them until the end of your next turn")
    void exilesTopTwoCardsWithPlayPermission() {
        Card first = new Divination();
        Card second = new Forest();
        castWatchWithLibrary(first, second);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd)
                .containsEntry(first.getId(), gd.turnNumber + 2)
                .containsEntry(second.getId(), gd.turnNumber + 2);
    }

    @Test
    @DisplayName("Adds two mana with a separate color choice for each mana")
    void addsTwoExileSpellOnlyMana() {
        harness.addToBattlefield(player1, new InterdimensionalWebWatch());
        Permanent watch = findPermanent(player1, "Interdimensional Web Watch");
        watch.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getTotal()).isZero();
        assertThat(pool.getExiledSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getExiledSpellOnlyMana(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exile-only mana pays for a spell cast from exile")
    void exileOnlyManaPaysForExiledSpell() {
        Card first = new Divination();
        castWatchWithLibrary(first, new Forest());
        Permanent watch = findPermanent(player1, "Interdimensional Web Watch");
        watch.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(first);
        assertThat(gd.playerManaPools.get(player1.getId()).getExiledSpellOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Exile-only mana cannot pay for a spell cast from hand")
    void exileOnlyManaCannotPayForHandSpell() {
        harness.addToBattlefield(player1, new InterdimensionalWebWatch());
        Permanent watch = findPermanent(player1, "Interdimensional Web Watch");
        watch.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getExiledSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);
    }

    private void castWatchWithLibrary(Card first, Card second) {
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new InterdimensionalWebWatch()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
