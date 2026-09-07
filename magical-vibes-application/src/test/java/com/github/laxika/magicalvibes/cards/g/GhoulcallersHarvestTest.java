package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhoulcallersHarvest.class, GrizzlyBears.class, Mountain.class})
class GhoulcallersHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Creates half the creature cards in the graveyard, rounded up, as decayed Zombies")
    void createsRoundedUpNumberOfDecayedZombies() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Mountain()));
        harness.setHand(player1, List.of(new GhoulcallersHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
                new GhoulcallersHarvest(), new GrizzlyBears(), new GrizzlyBears(), new Mountain()));
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

    private List<Permanent> findZombies() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Zombie".equals(permanent.getCard().getName()))
                .toList();
    }
}
