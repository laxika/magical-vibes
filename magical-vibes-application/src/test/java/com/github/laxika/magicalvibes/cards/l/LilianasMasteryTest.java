package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LilianasMasteryTest extends BaseCardTest {

    private void castMastery(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new LilianasMastery()));
        harness.addMana(player, ManaColor.BLACK, 5);
        harness.castEnchantment(player, 0);
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB token trigger
    }

    @Test
    @DisplayName("ETB creates two 2/2 black Zombie tokens")
    void etbCreatesTwoZombieTokens() {
        castMastery(player1);

        List<Permanent> zombieTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();

        assertThat(zombieTokens).hasSize(2);
        assertThat(zombieTokens).allMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Anthem boosts the created Zombie tokens to 3/3")
    void anthemBoostsCreatedZombieTokens() {
        castMastery(player1);

        List<Permanent> zombieTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();

        assertThat(zombieTokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Anthem boosts other Zombies you control")
    void anthemBoostsOtherOwnZombies() {
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());
        castMastery(player1);

        // Walking Corpse is a 2/2 Zombie -> 3/3 with the +1/+1
        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(3);
    }

    @Test
    @DisplayName("Anthem does not boost non-Zombie creatures")
    void anthemDoesNotBoostNonZombies() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castMastery(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Anthem does not boost opponent's Zombies")
    void anthemDoesNotBoostOpponentZombies() {
        Permanent opponentCorpse = addCreatureReady(player2, new WalkingCorpse());
        castMastery(player1);

        assertThat(gqs.getEffectivePower(gd, opponentCorpse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCorpse)).isEqualTo(2);
    }
}
