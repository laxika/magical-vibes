package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SettleTheWilds.class, Forest.class, ElvishMystic.class, GrizzlyBears.class, GiantGrowth.class, DarksteelIngot.class, Mutavault.class})
class SettleTheWildsTest extends BaseCardTest {

    @Test
    void seeksTappedBasicLandThenSeeksExactLandCountPermanentToHand() {
        Card forest = new Forest();
        Card mystic = new ElvishMystic();
        Card nonPermanent = new GiantGrowth();
        harness.setHand(player1, List.of(new SettleTheWilds()));
        harness.setLibrary(player1, List.of(forest, mystic, nonPermanent));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent enteredForest = findPermanent(player1, "Forest");
        assertThat(enteredForest.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(mystic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonPermanent);
    }

    @Test
    void doesNotSeekPermanentWithDifferentManaValue() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new SettleTheWilds()));
        harness.setLibrary(player1, List.of(forest, bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void seeksArtifactUsingOnlyControllersLandsAfterLandEnters() {
        Card forest = new Forest();
        Card ingot = new DarksteelIngot();
        Card mystic = new ElvishMystic();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new ElvishMystic());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SettleTheWilds()));
        harness.setLibrary(player1, List.of(forest, ingot, mystic));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ingot);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mystic);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
    }

    @Test
    void stillSeeksPermanentWhenNoBasicLandIsAvailable() {
        Card mystic = new ElvishMystic();
        Card growth = new GiantGrowth();
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SettleTheWilds()));
        harness.setLibrary(player1, List.of(growth, mystic));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mystic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(growth);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    void seeksNonbasicLandToHandWhenControllerHasNoLands() {
        Card mutavault = new Mutavault();
        Card mystic = new ElvishMystic();
        harness.setHand(player1, List.of(new SettleTheWilds()));
        harness.setLibrary(player1, List.of(mutavault, mystic));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mutavault);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mystic);
    }

    @Test
    void leavesUnselectedCardsInTheirOriginalLibraryOrder() {
        Card first = new GiantGrowth();
        Card forest = new Forest();
        Card second = new GrizzlyBears();
        Card mystic = new ElvishMystic();
        Card third = new GiantGrowth();
        harness.setHand(player1, List.of(new SettleTheWilds()));
        harness.setLibrary(player1, List.of(first, forest, second, mystic, third));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mystic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    void resolvesWithoutFindingAnythingInAnEmptyLibrary() {
        Card spell = new SettleTheWilds();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }
}
