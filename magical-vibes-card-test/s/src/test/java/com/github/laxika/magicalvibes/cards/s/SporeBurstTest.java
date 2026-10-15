package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuptureSpire;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SporeBurst.class, Forest.class, Plains.class, Swamp.class, Island.class, Mountain.class, RuptureSpire.class})
class SporeBurstTest extends BaseCardTest {

    private List<Permanent> saprolings(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling"))
                .toList();
    }

    private void castSporeBurst() {
        harness.setHand(player1, List.of(new SporeBurst()));
        harness.addMana(player1, ManaColor.GREEN, 4); // {3}{G}
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Creates a 1/1 green Saproling for each basic land type you control")
    void createsSaprolingPerBasicLandType() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());

        castSporeBurst();

        List<Permanent> tokens = saprolings(player1);
        assertThat(tokens).hasSize(3);

        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("Each basic land type is counted only once")
    void countsEachBasicLandTypeOnce() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        castSporeBurst();

        assertThat(saprolings(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Only the caster's lands count toward domain, not the opponent's")
    void countsOnlyControllersLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());

        castSporeBurst();

        assertThat(saprolings(player1)).hasSize(1);
    }

    @Test
    @DisplayName("With no basic lands, no tokens are created and the spell goes to the graveyard")
    void noBasicLandsCreatesNoTokens() {
        castSporeBurst();

        assertThat(saprolings(player1)).isEmpty();
        harness.assertInGraveyard(player1, "Spore Burst");
    }

    @Test
    @DisplayName("All five basic land types create five tokens")
    void allFiveBasicLandTypesCreateFiveTokens() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        castSporeBurst();

        assertThat(saprolings(player1)).hasSize(5);
        assertThat(saprolings(player2)).isEmpty();
    }

    @Test
    @DisplayName("A land that produces any color but has no basic land type creates no tokens")
    void landWithoutBasicLandTypeCreatesNoTokens() {
        harness.addToBattlefield(player1, new RuptureSpire());

        castSporeBurst();

        assertThat(saprolings(player1)).isEmpty();
        harness.assertInGraveyard(player1, "Spore Burst");
    }

    @Test
    @DisplayName("Domain counts lands at resolution")
    void countsBasicLandTypesAtResolution() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SporeBurst()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);

        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();

        assertThat(saprolings(player1)).hasSize(2);
    }
}
