package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NoviceOccultist;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({GhoulcallersHarvest.class, NoviceOccultist.class})
class GhoulcallersHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Creates half the creature cards in the graveyard, rounded up, as decayed Zombies")
    void createsRoundedUpNumberOfDecayedZombies() {
        harness.setGraveyard(player1, List.of(
                new NoviceOccultist(), new NoviceOccultist(), new NoviceOccultist(), new GhoulcallersHarvest()));
        harness.setHand(player1, List.of(new GhoulcallersHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> zombies = findZombies();
        assertThat(zombies).hasSize(2).allSatisfy(zombie -> {
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
            assertThat(zombie.getEffectivePower()).isEqualTo(2);
            assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
            assertThat(bls.canBlock(gd, zombie)).isFalse();
        });
    }

    @Test
    @DisplayName("Flashback creates the Zombies and exiles Ghoulcaller's Harvest")
    void flashbackCreatesZombiesAndExilesSelf() {
        harness.setGraveyard(player1, List.of(
                new GhoulcallersHarvest(), new NoviceOccultist(), new NoviceOccultist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findZombies()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Ghoulcaller's Harvest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ghoulcaller's Harvest"));
    }

    @Test
    @DisplayName("Creates no tokens when only the opponent has creature cards in a graveyard")
    void ignoresOpponentsCreaturesAndOwnNoncreatures() {
        harness.setGraveyard(player1, List.of(new GhoulcallersHarvest()));
        harness.setGraveyard(player2, List.of(
                new NoviceOccultist(), new NoviceOccultist(), new NoviceOccultist()));
        harness.setHand(player1, List.of(new GhoulcallersHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findZombies()).isEmpty();
        harness.assertInGraveyard(player1, "Ghoulcaller's Harvest");
    }

    @Test
    @DisplayName("Counts creature cards when the spell resolves rather than when it is cast")
    void countsCreaturesAtResolution() {
        harness.setGraveyard(player1, List.of(new NoviceOccultist()));
        harness.setHand(player1, List.of(new GhoulcallersHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.setGraveyard(player1, List.of(
                new NoviceOccultist(), new NoviceOccultist(), new NoviceOccultist()));
        harness.passBothPriorities();

        assertThat(findZombies()).hasSize(2);
    }

    @Test
    @DisplayName("Only the attacking decayed Zombie is sacrificed at end of combat")
    void sacrificesAttackingZombieAndKeepsNonattacker() {
        harness.setGraveyard(player1, List.of(
                new NoviceOccultist(), new NoviceOccultist(), new NoviceOccultist()));
        harness.setHand(player1, List.of(new GhoulcallersHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        List<Permanent> zombies = findZombies();
        assertThat(zombies).hasSize(2);
        Permanent attacker = zombies.getFirst();
        Permanent nonattacker = zombies.getLast();
        attacker.setSummoningSick(false);
        int initialLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, initialLife - 2);
        assertThat(findZombies()).containsExactly(nonattacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    private List<Permanent> findZombies() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Zombie".equals(permanent.getCard().getName()))
                .toList();
    }
}
