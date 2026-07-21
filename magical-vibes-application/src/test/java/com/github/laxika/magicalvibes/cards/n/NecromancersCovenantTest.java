package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NecromancersCovenantTest extends BaseCardTest {

    private void castCovenant(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new NecromancersCovenant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, targetPlayerId);
        harness.passBothPriorities(); // resolve enchantment -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private long zombieTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .count();
    }

    // ===== ETB: exile creature cards from target player's graveyard, make a Zombie per card =====

    @Test
    @DisplayName("ETB exiles all creature cards from target player's graveyard and creates a Zombie per card")
    void etbExilesCreaturesAndCreatesZombies() {
        Card bears = new GrizzlyBears();
        Card angel = new SerraAngel();
        Card plains = new Plains(); // non-creature, must remain
        harness.setGraveyard(player2, List.of(bears, angel, plains));

        castCovenant(player2.getId());

        // Only the non-creature card stays behind in the graveyard.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(plains.getId());

        // Both creature cards are exiled (to their owner's exile).
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), angel.getId());

        // Two 2/2 black Zombie tokens under the covenant's controller.
        assertThat(zombieTokenCount(player1)).isEqualTo(2);
        assertThat(zombieTokenCount(player2)).isZero();
    }

    @Test
    @DisplayName("ETB creates no Zombies when the target's graveyard has no creature cards")
    void noCreaturesCreatesNoZombies() {
        harness.setGraveyard(player2, List.of(new Plains(), new Plains()));

        castCovenant(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2); // untouched
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(zombieTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("ETB can target the controller's own graveyard")
    void canTargetOwnGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castCovenant(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(bears.getId());
        assertThat(zombieTokenCount(player1)).isEqualTo(1);
    }

    // ===== Static: Zombies you control have lifelink =====

    @Test
    @DisplayName("Grants lifelink only to Zombies you control")
    void grantsLifelinkToOwnZombiesOnly() {
        addReady(player1, new NecromancersCovenant());
        Permanent myZombie = addReady(player1, new WalkingCorpse());
        Permanent myNonZombie = addReady(player1, new GrizzlyBears());
        Permanent opponentZombie = addReady(player2, new WalkingCorpse());

        assertThat(gqs.hasKeyword(gd, myZombie, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, myNonZombie, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentZombie, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A Zombie you control gains life when it deals combat damage")
    void lifelinkZombieGainsLifeInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReady(player1, new NecromancersCovenant());
        Permanent zombie = addReady(player1, new WalkingCorpse()); // 2/2 Zombie
        zombie.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22); // lifelink
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
