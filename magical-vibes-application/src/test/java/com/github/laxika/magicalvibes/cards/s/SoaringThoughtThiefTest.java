package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.e.ExpeditionSkulker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.z.ZulaportDuelist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoaringThoughtThief.class, ExpeditionSkulker.class, ExpeditionHealer.class,
        ZulaportDuelist.class, Island.class})
class SoaringThoughtThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Rogues get +1/+0 while an opponent has eight cards in their graveyard")
    void boostsRoguesAtGraveyardThreshold() {
        Permanent thief = harness.addToBattlefieldAndReturn(player1, new SoaringThoughtThief());
        Permanent rogue = addCreatureReady(player1, new ExpeditionSkulker());
        Permanent nonRogue = addCreatureReady(player1, new ExpeditionHealer());
        int thiefBasePower = gqs.getEffectivePower(gd, thief);
        int rogueBasePower = gqs.getEffectivePower(gd, rogue);
        int nonRogueBasePower = gqs.getEffectivePower(gd, nonRogue);

        harness.setGraveyard(player2, graveyardOfSize(7));
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(thiefBasePower);
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(rogueBasePower);
        assertThat(gqs.getEffectivePower(gd, nonRogue)).isEqualTo(nonRogueBasePower);

        harness.setGraveyard(player2, graveyardOfSize(8));
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(thiefBasePower + 1);
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(rogueBasePower + 1);
        assertThat(gqs.getEffectivePower(gd, nonRogue)).isEqualTo(nonRogueBasePower);
    }

    @Test
    @DisplayName("Mills each opponent two cards when one or more Rogues attack")
    void millsEachOpponentOnceForMultipleRogues() {
        addCreatureReady(player1, new ZulaportDuelist());
        addCreatureReady(player1, new ExpeditionSkulker());
        addCreatureReady(player1, new ExpeditionSkulker());
        harness.addToBattlefield(player1, new SoaringThoughtThief());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not mill when only a non-Rogue attacks")
    void doesNotMillForNonRogueAttackers() {
        harness.addToBattlefield(player1, new SoaringThoughtThief());
        addCreatureReady(player1, new ExpeditionHealer());
        List<Card> library = List.of(new Island(), new Island());
        harness.setLibrary(player2, library);

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only opposing graveyards enable the bonus, which disappears below eight cards")
    void bonusTracksOpposingGraveyardAndOnlyBoostsOwnRogues() {
        Permanent thief = harness.addToBattlefieldAndReturn(player1, new SoaringThoughtThief());
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());
        Permanent opposingRogue = harness.addToBattlefieldAndReturn(player2, new ExpeditionSkulker());
        int thiefPower = gqs.getEffectivePower(gd, thief);
        int roguePower = gqs.getEffectivePower(gd, rogue);
        int rogueToughness = gqs.getEffectiveToughness(gd, rogue);
        int opposingPower = gqs.getEffectivePower(gd, opposingRogue);

        harness.setGraveyard(player1, graveyardOfSize(8));
        harness.setGraveyard(player2, graveyardOfSize(7));
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(thiefPower);
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(roguePower);

        harness.setGraveyard(player2, graveyardOfSize(9));
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(thiefPower + 1);
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(roguePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, rogue)).isEqualTo(rogueToughness);
        assertThat(gqs.getEffectivePower(gd, opposingRogue)).isEqualTo(opposingPower);

        harness.setGraveyard(player2, graveyardOfSize(7));
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(thiefPower);
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(roguePower);
    }

    @Test
    @DisplayName("The thief's own attack mills only its opponent and enables the bonus")
    void ownAttackMillsBeforeCombatAndEnablesBonus() {
        Permanent thief = addCreatureReady(player1, new SoaringThoughtThief());
        int basePower = gqs.getEffectivePower(gd, thief);
        harness.setGraveyard(player2, graveyardOfSize(6));
        List<Card> ownLibrary = List.of(new Island(), new Island());
        harness.setLibrary(player1, ownLibrary);
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(ownLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Each copy triggers once and its static bonus stacks")
    void multipleCopiesTriggerAndBoostIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SoaringThoughtThief());
        harness.addToBattlefield(player1, new SoaringThoughtThief());
        addCreatureReady(player1, new ExpeditionSkulker());
        int basePower = gqs.getEffectivePower(gd, first);
        harness.setGraveyard(player2, graveyardOfSize(4));
        harness.setLibrary(player2, graveyardOfSize(5));

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 2);
    }

    @Test
    @DisplayName("Opposing Rogue attacks do not trigger the thief")
    void opposingRogueAttackDoesNotTrigger() {
        harness.addToBattlefield(player1, new SoaringThoughtThief());
        addCreatureReady(player2, new ExpeditionSkulker());
        List<Card> library = List.of(new Island(), new Island());
        harness.setLibrary(player1, library);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The mill trigger survives the source and attacking Rogue leaving")
    void millStillResolvesAfterSourceAndAttackerLeave() {
        harness.addToBattlefield(player1, new SoaringThoughtThief());
        addCreatureReady(player1, new ExpeditionSkulker());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent with fewer than two library cards mills what remains")
    void millsShortLibrary() {
        addCreatureReady(player1, new SoaringThoughtThief());
        harness.setLibrary(player2, List.of(new Island()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }


    @Test
    @DisplayName("Several attacking Rogues produce only one mill trigger per thief")
    void multipleRoguesDoNotMultiplyMillAmount() {
        harness.addToBattlefield(player1, new SoaringThoughtThief());
        addCreatureReady(player1, new ExpeditionSkulker());
        addCreatureReady(player1, new ZulaportDuelist());
        harness.setLibrary(player2, graveyardOfSize(6));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    private List<Card> graveyardOfSize(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Island());
        }
        return cards;
    }
}
