package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaiusVanBaelsar.class, GrizzlyBears.class, GroundSeal.class})
class GaiusVanBaelsarTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 makes each player sacrifice a creature token")
    void sacrificesCreatureTokens() {
        Card player1Token = creatureToken("Player 1 Soldier Token");
        Card player2Token = creatureToken("Player 2 Soldier Token");
        harness.addToBattlefield(player1, player1Token);
        harness.addToBattlefield(player2, player2Token);

        castGaius(0);
        resolveCreatureAndEtb();

        assertThat(findPermanents(player1, "Player 1 Soldier Token")).isEmpty();
        assertThat(findPermanents(player2, "Player 2 Soldier Token")).isEmpty();
        harness.assertOnBattlefield(player1, "Gaius van Baelsar");
    }

    @Test
    @DisplayName("Mode 1 makes each player sacrifice a nontoken creature")
    void sacrificesNontokenCreatures() {
        Permanent player1Bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2FirstBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2SecondBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGaius(1);
        resolveCreatureAndEtb();

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Bears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(player2FirstBears.getId()));

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears")).containsExactly(player2SecondBears);
    }

    @Test
    @DisplayName("Mode 2 makes each player sacrifice an enchantment")
    void sacrificesEnchantments() {
        harness.addToBattlefield(player1, new GroundSeal());
        harness.addToBattlefield(player2, new GroundSeal());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());

        castGaius(2);
        resolveCreatureAndEtb();

        assertThat(findPermanents(player1, "Ground Seal")).isEmpty();
        assertThat(findPermanents(player2, "Ground Seal")).isEmpty();
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    private void castGaius(int mode) {
        harness.setHand(player1, List.of(new GaiusVanBaelsar()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, mode);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setToken(true);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SOLDIER));
        return card;
    }
}
