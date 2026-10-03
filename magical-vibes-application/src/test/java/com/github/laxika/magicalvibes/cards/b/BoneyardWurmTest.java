package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.w.WoodenStake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneyardWurm.class, WalkingCorpse.class, Plains.class, WoodenStake.class})
class BoneyardWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Boneyard Wurm puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new BoneyardWurm(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BoneyardWurm.class);
    }

    @Test
    @DisplayName("Resolving Boneyard Wurm puts it on the battlefield when graveyard has creatures")
    void resolvingPutsItOnBattlefield() {
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.castFromHand(player1, new BoneyardWurm(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Boneyard Wurm");
    }

    @Test
    @DisplayName("Boneyard Wurm dies to state-based actions when no creatures in controller's graveyard")
    void diesWhenNoCreaturesInGraveyard() {
        harness.castFromHand(player1, new BoneyardWurm(), "{1}{G}");
        harness.passBothPriorities();

        // 0/0 creature dies to SBA — but then Boneyard Wurm itself is a creature card in the graveyard,
        // making it effectively a 1/1 while in play next time. However, SBA checks P/T on the battlefield
        // where it's 0/0 before going to the graveyard.
        harness.assertNotOnBattlefield(player1, "Boneyard Wurm");
        harness.assertInGraveyard(player1, "Boneyard Wurm");
    }

    @Test
    @DisplayName("Boneyard Wurm is 0/0 with no creature cards in controller's graveyard")
    void isZeroZeroWithEmptyGraveyard() {
        Permanent perm = addBoneyardWurmReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Boneyard Wurm P/T equals number of creature cards in controller's graveyard")
    void ptEqualsCreatureCountInOwnGraveyard() {
        Permanent perm = addBoneyardWurmReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boneyard Wurm does NOT count creature cards in opponent's graveyard")
    void doesNotCountOpponentsGraveyard() {
        Permanent perm = addBoneyardWurmReady(player1);
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Boneyard Wurm only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        Permanent perm = addBoneyardWurmReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(2));
        graveyard.add(new Plains());
        graveyard.add(new WoodenStake());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boneyard Wurm P/T updates when creatures are added to graveyard")
    void ptUpdatesWhenCreaturesAddedToGraveyard() {
        Permanent perm = addBoneyardWurmReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new WalkingCorpse());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boneyard Wurm counts only controller's graveyard when both have creatures")
    void countsOnlyControllerGraveyardWhenBothHaveCreatures() {
        Permanent perm = addBoneyardWurmReady(player1);
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.setGraveyard(player2, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boneyard Wurm shrinks as creature cards leave the graveyard")
    void shrinksWhenCreatureCardsLeaveGraveyard() {
        harness.setGraveyard(player1, createCreatureCards(3));
        Permanent perm = addBoneyardWurmReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);

        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        harness.setGraveyard(player1, List.of());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Boneyard Wurm");
        harness.assertInGraveyard(player1, "Boneyard Wurm");
    }

    @Test
    @DisplayName("Boneyard Wurm counts itself in its owner's graveyard")
    void countsItselfInGraveyard() {
        BoneyardWurm wurm = new BoneyardWurm();
        harness.setGraveyard(player1, List.of(wurm, new WalkingCorpse(), new Plains()));
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectiveCardPower(gd, wurm)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, wurm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boneyard Wurm's characteristic ability works in hand")
    void characteristicAbilityWorksInHand() {
        BoneyardWurm wurm = new BoneyardWurm();
        harness.setHand(player1, List.of(wurm));
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectiveCardPower(gd, wurm)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, wurm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boneyard Wurm uses its new controller's graveyard after changing control")
    void usesNewControllersGraveyard() {
        harness.setGraveyard(player1, createCreatureCards(1));
        harness.setGraveyard(player2, createCreatureCards(3));
        Permanent perm = addBoneyardWurmReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerBattlefields.get(player2.getId()).add(perm);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    private Permanent addBoneyardWurmReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BoneyardWurm());
        perm.setSummoningSick(false);
        return perm;
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new WalkingCorpse());
        }
        return creatures;
    }
}
