package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cubwarden.class})
class CubwardenTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating creates two 1/1 white Cat tokens with lifelink")
    void mutatingCreatesTwoLifelinkCats() {
        Permanent cubwarden = addCreatureReady(player1, new Cubwarden());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, cubwarden, List.of(cubwarden.getCard()), player1.getId()));
        resolveAllTriggers();

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).allSatisfy(cat -> {
            assertThat(cat.getCard().getPower()).isEqualTo(1);
            assertThat(cat.getCard().getToughness()).isEqualTo(1);
            assertThat(cat.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(cat.getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
            assertThat(cat.getCard().getKeywords()).contains(Keyword.LIFELINK);
            assertThat(cat.getCard().isToken()).isTrue();
        });
    }

    @Test
    void canCastForMutateCostTargetingOwnedNonHumanCreature() {
        Permanent target = addCreatureReady(player1, new Cubwarden());
        harness.setHand(player1, List.of(new Cubwarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castWithAlternateCost(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(countPermanents(player1, "Cubwarden")).isEqualTo(1);
    }

    @Test
    void castingNormallyDoesNotCreateCats() {
        harness.castFromHand(player1, new Cubwarden(), "{3}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cubwarden");
        assertThat(findPermanents(player1, "Cat")).isEmpty();
        assertThat(findPermanents(player2, "Cat")).isEmpty();
    }

    @Test
    void eachMutationCreatesTwoMoreCats() {
        Permanent cubwarden = addCreatureReady(player1, new Cubwarden());

        for (int mutation = 1; mutation <= 3; mutation++) {
            harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, cubwarden, List.of(cubwarden.getCard()), player1.getId()));
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Cat")).isEqualTo(2L * mutation);
        }
    }

    @Test
    void mutationCreatesCatsForTheCreatureController() {
        Permanent cubwarden = addCreatureReady(player2, new Cubwarden());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, cubwarden, List.of(cubwarden.getCard()), player2.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Cat")).isEqualTo(2);
        assertThat(findPermanents(player1, "Cat")).isEmpty();
    }

    @Test
    void catTokensGainLifeWhenTheyDealCombatDamage() {
        Permanent cubwarden = addCreatureReady(player1, new Cubwarden());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, cubwarden, List.of(cubwarden.getCard()), player1.getId()));
        resolveAllTriggers();
        findPermanents(player1, "Cat").forEach(cat -> cat.setSummoningSick(false));

        declareAttackers(List.of(1, 2));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
