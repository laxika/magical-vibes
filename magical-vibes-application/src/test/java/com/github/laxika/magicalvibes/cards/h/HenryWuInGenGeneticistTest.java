package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HenryWuInGenGeneticist.class, EliteVanguard.class, GrizzlyBears.class, AirElemental.class})
class HenryWuInGenGeneticistTest extends BaseCardTest {

    @Test
    @DisplayName("Human creatures you control gain exploit")
    void humanCreaturesGainExploit() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addToBattlefield(player1, new HenryWuInGenGeneticist());
        castHumanCreature();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Elite Vanguard");
    }

    @Test
    @DisplayName("Draws for exploiting a non-Human and creates a Treasure at power 3 or greater")
    void drawsAndCreatesTreasureForLargeNonHuman() {
        AirElemental fodder = new AirElemental();
        Permanent fodderPermanent = harness.addToBattlefieldAndReturn(player1, fodder);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        castHenry();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodderPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(treasureTokens()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Does not draw when the exploited creature is Human")
    void ignoresHumanExploitedCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        castHenry();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(treasureTokens()).isZero();
    }

    private void castHenry() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HenryWuInGenGeneticist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void castHumanCreature() {
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private long treasureTokens() {
        return findPermanents(player1, "Treasure").stream()
                .filter(permanent -> permanent.getCard().isToken()).count();
    }

    @Test
    void henryKeepsGrantedExploitWhenNoLongerHuman() {
        Permanent henry = harness.addToBattlefieldAndReturn(player1, new HenryWuInGenGeneticist());
        henry.setTransientCreatureTypeOverride(CardSubtype.BEAR);
        assertThat(gqs.hasKeyword(gd, henry, Keyword.EXPLOIT)).isTrue();
    }

    @Test
    void smallNonHumanDrawsWithoutTreasure() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        castHenry();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(treasureTokens()).isZero();
    }

    @Test
    void powerAtSacrificeIncludesCounters() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        fodder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        castHenry();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(treasureTokens()).isEqualTo(1);
    }

    @Test
    void decliningExploitDoesNotSacrificeOrReward() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castHenry();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(treasureTokens()).isZero();
    }

    @Test
    void creatureThatBecameHumanDoesNotGiveReward() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        fodder.getGrantedSubtypes().add(CardSubtype.HUMAN);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castHenry();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(treasureTokens()).isZero();
    }

    @Test
    void humanThatLostHumanTypeGivesReward() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        fodder.setTransientCreatureTypeOverride(CardSubtype.BEAR);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        castHenry();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(treasureTokens()).isZero();
    }
}
