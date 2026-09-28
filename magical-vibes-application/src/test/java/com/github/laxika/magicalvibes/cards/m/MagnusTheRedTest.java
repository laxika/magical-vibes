package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MagnusTheRed.class, Divination.class, GrizzlyBears.class})
class MagnusTheRedTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells cost less for each creature token you control")
    void reducesInstantAndSorceryCostsForCreatureTokens() {
        harness.addToBattlefield(player1, new MagnusTheRed());
        harness.addToBattlefield(player1, tokenCreature());
        harness.addToBattlefield(player1, tokenCreature());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);

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

    private GrizzlyBears tokenCreature() {
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        return token;
    }
}
