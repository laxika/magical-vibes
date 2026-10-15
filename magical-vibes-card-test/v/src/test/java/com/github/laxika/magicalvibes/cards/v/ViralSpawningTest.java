package com.github.laxika.magicalvibes.cards.v;

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

@CardUsed({ViralSpawning.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveFlashback(player1, 0, null);

        Permanent beast = findPermanent(player1, "Phyrexian Beast");
        beast.setSummoningSick(false);
        declareAttackers(player1, List.of(0));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spawning.getId()));
    }

    @Test
    @DisplayName("Toxic gives poison counters with combat damage without using the stack")
    void toxicIsImmediateCombatDamageResult() {
        harness.setHand(player1, List.of(new ViralSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent beast = findPermanent(player1, "Phyrexian Beast");
        beast.setSummoningSick(false);
        beast.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing corrupted after casting does not stop flashback resolution or exile")
    void losingCorruptedAfterCastingDoesNotStopResolution() {
        ViralSpawning spawning = new ViralSpawning();
        harness.setGraveyard(player1, List.of(spawning));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Beast");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spawning);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spawning);
    }

    @Test
    @DisplayName("Corrupted flashback still requires paying two generic and one green mana")
    void flashbackRequiresFullManaCost() {
        ViralSpawning spawning = new ViralSpawning();
        harness.setGraveyard(player1, List.of(spawning));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spawning);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Phyrexian Beast");
    }

}
