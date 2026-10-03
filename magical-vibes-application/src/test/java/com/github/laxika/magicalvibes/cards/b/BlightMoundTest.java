package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
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

        harness.castAndResolveInstant(player1, 0, creature.getId());
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
        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent pest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .findFirst()
                .orElseThrow();
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("An opposing nontoken creature dying does not create a Pest")
    void opposingCreatureDeathDoesNotCreatePest() {
        harness.addToBattlefield(player1, new BlightMound());
        Permanent creature = addCreature(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("A generated Pest dying grants life without creating another Pest")
    void tokenDeathDoesNotCreateAnotherPest() {
        harness.addToBattlefield(player1, new BlightMound());
        Permanent pest = createPestThroughCreatureDeath();
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Generated Pests receive the bonus only while attacking, and other creatures do not")
    void generatedPestBonusTracksAttackingState() {
        harness.addToBattlefield(player1, new BlightMound());
        Permanent pest = createPestThroughCreatureDeath();
        Permanent bear = addCreature(player1);
        bear.setAttacking(true);

        assertThat(pest.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pest, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isFalse();

        pest.setAttacking(true);
        assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pest, Keyword.MENACE)).isTrue();

        pest.setAttacking(false);
        assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pest, Keyword.MENACE)).isFalse();
    }

    private Permanent createPestThroughCreatureDeath() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .findFirst()
                .orElseThrow();
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
