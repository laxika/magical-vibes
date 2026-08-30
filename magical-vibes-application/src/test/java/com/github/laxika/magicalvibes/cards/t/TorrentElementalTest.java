package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TorrentElemental.class, GrizzlyBears.class})
class TorrentElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps all creatures controlled by the defending player")
    void attackingTapsDefendingCreatures() {
        TorrentElemental torrent = new TorrentElemental();
        addCreatureReady(player1, torrent);
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exile ability returns Torrent Elemental to the battlefield tapped")
    void returnsFromExileTapped() {
        TorrentElemental torrent = new TorrentElemental();
        harness.setExile(player1, List.of(torrent));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateExileAbility(player1, torrent.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(torrent.getId())).isNull();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(torrent.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exile ability can only be activated at sorcery speed")
    void exileAbilityOnlyAtSorcerySpeed() {
        TorrentElemental torrent = new TorrentElemental();
        harness.setExile(player1, List.of(torrent));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateExileAbility(player1, torrent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.findExiledCard(torrent.getId())).isNotNull();
    }
}
