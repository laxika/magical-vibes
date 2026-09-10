package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpeditionChampion.class, GrizzlyBears.class})
class ExpeditionChampionTest extends BaseCardTest {

    @Test
    void hasBaseStatsWithoutAnotherWarrior() {
        harness.addToBattlefield(player1, new ExpeditionChampion());

        Permanent champion = findPermanent(player1, "Expedition Champion");
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    @Test
    void getsPlusTwoPowerWithAnotherWarrior() {
        harness.addToBattlefield(player1, new ExpeditionChampion());
        harness.addToBattlefield(player1, createWarrior());

        Permanent champion = findPermanent(player1, "Expedition Champion");
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    @Test
    void opponentWarriorDoesNotCount() {
        harness.addToBattlefield(player1, new ExpeditionChampion());
        harness.addToBattlefield(player2, createWarrior());

        Permanent champion = findPermanent(player1, "Expedition Champion");
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
    }

    @Test
    void losesBonusWhenAnotherWarriorLeaves() {
        harness.addToBattlefield(player1, new ExpeditionChampion());
        harness.addToBattlefield(player1, createWarrior());

        Permanent champion = findPermanent(player1, "Expedition Champion");
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.WARRIOR)
                        && !permanent.getCard().getName().equals("Expedition Champion"));

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
    }

    private Card createWarrior() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.WARRIOR));
        return card;
    }
}
