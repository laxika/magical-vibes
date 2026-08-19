package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViralSpawningTest extends BaseCardTest {

    @Test
    @DisplayName("Flashback requires an opponent with three poison counters")
    void flashbackRequiresCorruptedOpponent() {
        ViralSpawning spawning = new ViralSpawning();
        harness.setGraveyard(player1, List.of(spawning));
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spawning);
    }

    @Test
    @DisplayName("Normal casting creates a toxic Phyrexian Beast")
    void normalCastCreatesBeast() {
        harness.setHand(player1, List.of(new ViralSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Phyrexian Beast");
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.TOXIC)).isTrue();
    }

    @Test
    @DisplayName("Corrupted flashback creates the Beast, poisons the opponent, and exiles itself")
    void corruptedFlashbackCreatesBeastAndPoisonsOpponent() {
        ViralSpawning spawning = new ViralSpawning();
        harness.setGraveyard(player1, List.of(spawning));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Phyrexian Beast");
        beast.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        declareAttackers(player1, List.of(0));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spawning.getId()));
    }

}
