package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightMound.class, GrizzlyBears.class, Shock.class})
class BlightMoundTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Pests you control get +1/+0 and menace")
    void attackingPestsYouControlGetBoostAndMenace() {
        harness.addToBattlefield(player1, new BlightMound());
        Permanent attackingPest = addPest(player1);
        attackingPest.setAttacking(true);
        Permanent nonattackingPest = addPest(player1);
        Permanent opposingPest = addPest(player2);
        opposingPest.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, attackingPest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attackingPest, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonattackingPest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonattackingPest, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingPest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingPest, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("A nontoken creature you control dying creates a Pest")
    void nontokenCreatureYouControlDyingCreatesPest() {
        harness.addToBattlefield(player1, new BlightMound());
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .hasSize(1);
    }

    @Test
    @DisplayName("A Pest created by Blight Mound gains 1 life when it dies")
    void createdPestGainsLifeWhenItDies() {
        harness.addToBattlefield(player1, new BlightMound());
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent pest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .findFirst()
                .orElseThrow();
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, pest.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    private Permanent addPest(com.github.laxika.magicalvibes.model.Player player) {
        Permanent pest = addCreature(player);
        TestCards.mutableCard(pest).setSubtypes(List.of(CardSubtype.PEST));
        return pest;
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
