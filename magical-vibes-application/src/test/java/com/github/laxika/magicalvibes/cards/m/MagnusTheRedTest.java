package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagnusTheRed.class, Divination.class, GrizzlyBears.class, Cancel.class, SolRing.class})
class MagnusTheRedTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells cost less for each creature token you control")
    void reducesInstantAndSorceryCostsForCreatureTokens() {
        harness.addToBattlefield(player1, new MagnusTheRed());
        harness.addToBattlefield(player1, tokenCreature());
        harness.addToBattlefield(player1, tokenCreature());
        harness.castFromHand(player1, new Divination(), "{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cost reduction ignores nontokens and tokens controlled by opponents")
    void ignoresNontokensAndOpponentsTokens() {
        harness.addToBattlefield(player1, new MagnusTheRed());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, tokenCreature());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Combat damage to a player creates a 3/3 red Spawn token")
    void createsSpawnOnCombatDamageToPlayer() {
        Permanent magnus = addCreatureReady(player1, new MagnusTheRed());
        magnus.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(findPermanents(player1, "Spawn")).singleElement().satisfies(spawn -> {
            assertThat(spawn.getCard().getPower()).isEqualTo(3);
            assertThat(spawn.getCard().getToughness()).isEqualTo(3);
            assertThat(spawn.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(spawn.getCard().getSubtypes()).contains(CardSubtype.SPAWN);
        });
    }

    @Test
    @DisplayName("Creature tokens reduce instant costs while preserving colored requirements")
    void reducesInstantCost() {
        harness.addToBattlefield(player1, new MagnusTheRed());
        harness.addToBattlefield(player1, tokenCreature());
        harness.addToBattlefield(player1, tokenCreature());
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination, new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.castInstant(player1, 0, divination.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player1, "Cancel");
    }

    @Test
    @DisplayName("Excess creature tokens cannot pay colored mana requirements")
    void doesNotReduceColoredMana() {
        harness.addToBattlefield(player1, new MagnusTheRed());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, tokenCreature());
        }
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Noncreature tokens do not reduce casting costs")
    void ignoresNoncreatureTokens() {
        harness.addToBattlefield(player1, new MagnusTheRed());
        SolRing artifactToken = new SolRing();
        artifactToken.setToken(true);
        harness.addToBattlefield(player1, artifactToken);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creature spells do not receive the cost reduction")
    void doesNotReduceCreatureSpellCosts() {
        harness.addToBattlefield(player1, new MagnusTheRed());
        harness.addToBattlefield(player1, tokenCreature());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Magnus does not reduce an opponent's spells even when that opponent controls tokens")
    void doesNotReduceOpponentsSpells() {
        harness.addToBattlefield(player2, new MagnusTheRed());
        harness.addToBattlefield(player1, tokenCreature());
        harness.addToBattlefield(player1, tokenCreature());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not create a Spawn")
    void doesNotTriggerWhenBlocked() {
        Permanent magnus = addCreatureReady(player1, new MagnusTheRed());
        magnus.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MagnusTheRed());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Spawn")).isEmpty();
        assertThat(findPermanents(player2, "Spawn")).isEmpty();
    }

    @Test
    @DisplayName("The Spawn trigger resolves after Magnus leaves the battlefield")
    void spawnTriggerSurvivesSourceLeaving() {
        Permanent magnus = addCreatureReady(player1, new MagnusTheRed());
        magnus.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(magnus);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spawn")).hasSize(1);
        assertThat(findPermanents(player2, "Spawn")).isEmpty();
    }

    private GrizzlyBears tokenCreature() {
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        return token;
    }
}
