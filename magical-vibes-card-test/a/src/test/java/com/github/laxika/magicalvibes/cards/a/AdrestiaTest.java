package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Adrestia.class, AssassinInitiate.class, AmoeboidChangeling.class, GrizzlyBears.class,
        ArcaneAdaptation.class, Island.class})
class AdrestiaTest extends BaseCardTest {

    @Test
    @CardUsed({Adrestia.class, AssassinInitiate.class, GrizzlyBears.class})
    @DisplayName("Assassin crewing draws a card and makes Adrestia an Assassin until end of turn")
    void assassinCrewTriggersOnAttack() {
        Permanent adrestia = addAdrestiaReady();
        addCreatureReady(player1, new AssassinInitiate());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        crewAdrestia();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isFalse();
    }

    @Test
    @CardUsed({Adrestia.class, GrizzlyBears.class})
    @DisplayName("Crewing with a non-Assassin does not trigger Adrestia")
    void nonAssassinCrewDoesNotTrigger() {
        Permanent adrestia = addAdrestiaReady();
        addCreatureReady(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        crewAdrestia();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isFalse();
    }

    @Test
    @CardUsed({Adrestia.class, AssassinInitiate.class, AmoeboidChangeling.class, GrizzlyBears.class})
    @DisplayName("Adrestia remembers the Assassin even after it loses its creature types")
    void remembersAssassinThatStopsBeingAssassin() {
        Permanent adrestia = addAdrestiaReady();
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());

        crewAdrestia();
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.activateAbility(player1, 2, 1, null, assassin.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, assassin)).doesNotContain(CardSubtype.ASSASSIN);

        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isTrue();
    }

    @Test
    @CardUsed({Adrestia.class, AssassinInitiate.class, ArcaneAdaptation.class})
    @DisplayName("Becoming an Assassin preserves creature types granted before the attack trigger")
    void becomingAssassinPreservesOtherCreatureTypes() {
        Permanent adrestia = addAdrestiaReady();
        addCreatureReady(player1, new AssassinInitiate());
        crewAdrestia();

        harness.castFromHand(player1, new ArcaneAdaptation(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.GOBLIN)).isTrue();

        Card drawn = new AssassinInitiate();
        harness.setLibrary(player1, List.of(drawn));
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.VEHICLE)).isTrue();
    }

    @Test
    @CardUsed({Adrestia.class, AmoeboidChangeling.class, AssassinInitiate.class})
    @DisplayName("A changeling counts as an Assassin when crewing Adrestia")
    void changelingCrewTriggersOnAttack() {
        Permanent adrestia = addAdrestiaReady();
        addCreatureReady(player1, new AmoeboidChangeling());
        Card drawn = new AssassinInitiate();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        crewAdrestia();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isTrue();
    }

    @Test
    @CardUsed({Adrestia.class, AssassinInitiate.class, Island.class})
    @DisplayName("Islandwalk prevents blocking only while the defender controls an Island")
    void islandwalkChecksDefendersLands() {
        Permanent adrestia = addAdrestiaReady();
        addCreatureReady(player1, new AssassinInitiate());
        Permanent blocker = addCreatureReady(player2, new AssassinInitiate());
        crewAdrestia();

        assertThat(bls.canBlockAttacker(gd, blocker, adrestia,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        harness.addToBattlefield(player1, new Island());
        assertThat(bls.canBlockAttacker(gd, blocker, adrestia,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        harness.addToBattlefield(player2, new Island());
        assertThat(bls.canBlockAttacker(gd, blocker, adrestia,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    private Permanent addAdrestiaReady() {
        return addCreatureReady(player1, new Adrestia());
    }

    private void crewAdrestia() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
