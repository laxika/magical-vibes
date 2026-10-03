package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivineSacrament.class, AvenFlock.class, DiligentFarmhand.class, Opalescence.class})
class DivineSacramentTest extends BaseCardTest {

    @Test
    void boostsWhiteCreaturesControlledByEitherPlayer() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player1, new AvenFlock());
        harness.addToBattlefield(player2, new AvenFlock());
        harness.addToBattlefield(player1, new DiligentFarmhand());
        harness.addToBattlefield(player2, new DiligentFarmhand());

        assertStats(player1, "Aven Flock", 3, 4);
        assertStats(player2, "Aven Flock", 3, 4);
        assertStats(player1, "Diligent Farmhand", 1, 1);
        assertStats(player2, "Diligent Farmhand", 1, 1);
    }

    @Test
    void thresholdAddsAnAdditionalBoostUsingTheControllersGraveyard() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player1, new AvenFlock());
        harness.addToBattlefield(player2, new AvenFlock());
        harness.addToBattlefield(player2, new DiligentFarmhand());

        fillGraveyard(player2, 7);
        assertStats(player1, "Aven Flock", 3, 4);
        assertStats(player2, "Aven Flock", 3, 4);
        assertStats(player2, "Diligent Farmhand", 1, 1);

        fillGraveyard(player1, 7);
        assertStats(player1, "Aven Flock", 4, 5);
        assertStats(player2, "Aven Flock", 4, 5);
        assertStats(player2, "Diligent Farmhand", 1, 1);
    }

    @Test
    void thresholdBoostDisappearsBelowSevenCards() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player1, new AvenFlock());
        fillGraveyard(player1, 7);

        assertStats(player1, "Aven Flock", 4, 5);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertStats(player1, "Aven Flock", 3, 4);
    }

    @Test
    void thresholdStartsAtSevenAndContinuesAboveSeven() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player1, new AvenFlock());
        fillGraveyard(player1, 6);

        assertStats(player1, "Aven Flock", 3, 4);

        fillGraveyard(player1, 7);
        assertStats(player1, "Aven Flock", 4, 5);

        fillGraveyard(player1, 8);
        assertStats(player1, "Aven Flock", 4, 5);
    }

    @Test
    void multipleSacramentsUseTheirOwnControllersThresholds() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player2, new DivineSacrament());
        harness.addToBattlefield(player1, new AvenFlock());
        harness.addToBattlefield(player2, new AvenFlock());
        fillGraveyard(player1, 7);

        assertStats(player1, "Aven Flock", 5, 6);
        assertStats(player2, "Aven Flock", 5, 6);

        fillGraveyard(player2, 7);
        assertStats(player1, "Aven Flock", 6, 7);
        assertStats(player2, "Aven Flock", 6, 7);
    }

    @Test
    void boostsEndWhenSacramentLeavesTheBattlefield() {
        Permanent sacrament = harness.addToBattlefieldAndReturn(player1, new DivineSacrament());
        harness.addToBattlefield(player2, new AvenFlock());
        fillGraveyard(player1, 7);

        assertStats(player2, "Aven Flock", 4, 5);

        gd.playerBattlefields.get(player1.getId()).remove(sacrament);

        assertStats(player2, "Aven Flock", 2, 3);
    }

    @Test
    @CardUsed({DivineSacrament.class, Opalescence.class})
    void animatedSacramentReceivesItsOwnBaseBoost() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player1, new Opalescence());

        assertStats(player1, "Divine Sacrament", 4, 4);
    }

    @Test
    @CardUsed({DivineSacrament.class, Opalescence.class, DiligentFarmhand.class})
    void animatedSacramentReceivesItsOwnThresholdBoost() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player1, new Opalescence());
        fillGraveyard(player1, 7);

        assertStats(player1, "Divine Sacrament", 5, 5);
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new DiligentFarmhand());
        }
        harness.setGraveyard(player, cards);
    }

    private void assertStats(Player player, String cardName, int power, int toughness) {
        Permanent creature = findPermanent(player, cardName);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness);
    }
}
