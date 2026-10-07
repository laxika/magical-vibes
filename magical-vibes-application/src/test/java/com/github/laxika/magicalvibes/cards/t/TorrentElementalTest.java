package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RealityShift;
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

@CardUsed({TorrentElemental.class, RealityShift.class})
class TorrentElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps all creatures controlled by the defending player")
    void attackingTapsDefendingCreatures() {
        TorrentElemental torrent = new TorrentElemental();
        addCreatureReady(player1, torrent);
        Permanent defendingCreature = addCreatureReady(player2, new TorrentElemental());

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
        Permanent returned = findPermanent(player1, "Torrent Elemental");
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    void attackTriggerStillTapsDefendersAfterSourceIsExiled() {
        Permanent attacker = addCreatureReady(player1, new TorrentElemental());
        Permanent defender = addCreatureReady(player2, new TorrentElemental());
        harness.setHand(player2, List.of(new RealityShift()));
        harness.setLibrary(player1, List.of(new TorrentElemental()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(attacker.getCard().getId())).isNotNull();
        assertThat(defender.isTapped()).isTrue();
    }

    @Test
    void attackTriggerTapsEveryDefenderButLeavesFriendlyCreaturesUntapped() {
        addCreatureReady(player1, new TorrentElemental());
        Permanent friendly = addCreatureReady(player1, new TorrentElemental());
        Permanent firstDefender = addCreatureReady(player2, new TorrentElemental());
        Permanent secondDefender = addCreatureReady(player2, new TorrentElemental());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(firstDefender.isTapped()).isTrue();
        assertThat(secondDefender.isTapped()).isTrue();
        assertThat(friendly.isTapped()).isFalse();
    }

    @Test
    void exileAbilityCannotBeActivatedDuringCombat() {
        TorrentElemental torrent = new TorrentElemental();
        harness.setExile(player1, List.of(torrent));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateExileAbility(player1, torrent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.findExiledCard(torrent.getId())).isNotNull();
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
