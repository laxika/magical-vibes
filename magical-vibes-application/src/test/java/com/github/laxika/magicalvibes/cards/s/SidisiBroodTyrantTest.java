package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SidisiBroodTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("When Sidisi enters, it mills three and creates one Zombie if a creature was milled")
    void entersMillsAndCreatesOneZombieForCreatureCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        castSidisi();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Milling multiple creature cards in one event creates only one Zombie")
    void createsOnlyOneZombiePerMillEvent() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castSidisi();

        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Milling no creature cards does not create a Zombie")
    void doesNotCreateZombieForNoncreatureCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castSidisi();

        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Attacking Sidisi mills three and triggers the Zombie ability")
    void attackMillsAndCreatesZombie() {
        addCreatureReady(player1, new SidisiBroodTyrant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(zombieTokens(player1)).hasSize(1);
    }

    private void castSidisi() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SidisiBroodTyrant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private List<Permanent> zombieTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Zombie"))
                .toList();
    }
}
