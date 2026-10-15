package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantCaterpillar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({PhyresisRoach.class, GiantCaterpillar.class, GrizzlyBears.class, Zombify.class})
class PhyresisRoachTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage perpetually grants toxic to Insects in all specified zones")
    void combatDamageGrantsToxicToInsectsAcrossZones() {
        Permanent roach = addCreatureReady(player1, new PhyresisRoach());
        Permanent battlefieldInsect = addCreatureReady(player1, new GiantCaterpillar());
        Permanent nonInsect = addCreatureReady(player1, new GrizzlyBears());
        GiantCaterpillar handInsect = new GiantCaterpillar();
        GiantCaterpillar libraryInsect = new GiantCaterpillar();
        GiantCaterpillar graveyardInsect = new GiantCaterpillar();

        harness.setHand(player1, List.of(handInsect));
        harness.setLibrary(player1, List.of(libraryInsect));
        harness.setGraveyard(player1, List.of(graveyardInsect));
        roach.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, battlefieldInsect, Keyword.TOXIC)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonInsect, Keyword.TOXIC)).isFalse();

        castCreatureAndResolve(handInsect);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        Card drawnInsect = gd.playerHands.get(player1.getId()).getFirst();
        castCreatureAndResolve(drawnInsect);

        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, graveyardInsect.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(handInsect.getId())
                        || permanent.getCard().getId().equals(libraryInsect.getId())
                        || permanent.getCard().getId().equals(graveyardInsect.getId()))
                .hasSize(3)
                .allSatisfy(permanent -> assertThat(gqs.hasKeyword(gd, permanent, Keyword.TOXIC)).isTrue());
    }

    @Test
    @DisplayName("Printed toxic 1 gives a poison counter on combat damage")
    void printedToxicGivesPoisonCounter() {
        Permanent roach = addCreatureReady(player1, new PhyresisRoach());

        dealCombatDamage(roach);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Roach gains another toxic 1 from its own trigger")
    void roachGainsToxicFromItsOwnTrigger() {
        Permanent roach = addCreatureReady(player1, new PhyresisRoach());
        dealCombatDamage(roach);
        int poisonBefore = gd.playerPoisonCounters.getOrDefault(player2.getId(), 0);

        dealCombatDamage(roach);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0) - poisonBefore).isEqualTo(2);
    }

    @Test
    @DisplayName("An Insect gains a cumulative toxic 1 from each trigger")
    void repeatedGrantsAccumulatePoisonValue() {
        Permanent roach = addCreatureReady(player1, new PhyresisRoach());
        Permanent insect = addCreatureReady(player1, new GiantCaterpillar());
        dealCombatDamage(roach);
        dealCombatDamage(roach);
        roach.setAttacking(false);
        int poisonBefore = gd.playerPoisonCounters.getOrDefault(player2.getId(), 0);

        dealCombatDamage(insect);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0) - poisonBefore).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger does not grant toxic to opposing Insects")
    void opposingInsectsDoNotGainToxic() {
        Permanent roach = addCreatureReady(player1, new PhyresisRoach());
        Permanent opposingInsect = addCreatureReady(player2, new GiantCaterpillar());
        GiantCaterpillar opposingHand = new GiantCaterpillar();
        harness.setHand(player2, List.of(opposingHand));

        dealCombatDamage(roach);

        assertThat(gqs.hasKeyword(gd, opposingInsect, Keyword.TOXIC)).isFalse();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, opposingHand, "{3}{G}");
        harness.passBothPriorities();
        assertThat(findPermanents(player2, "Giant Caterpillar"))
                .hasSize(2)
                .allSatisfy(permanent -> assertThat(gqs.hasKeyword(gd, permanent, Keyword.TOXIC)).isFalse());
    }

    private void dealCombatDamage(Permanent attacker) {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        attacker.setAttacking(false);
    }

    private void castCreatureAndResolve(Card card) {
        harness.castFromHand(player1, card, "{3}{G}");
        harness.passBothPriorities();
    }
}
