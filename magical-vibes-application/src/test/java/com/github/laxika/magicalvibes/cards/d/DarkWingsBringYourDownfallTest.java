package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkWingsBringYourDownfall.class, GrizzlyBears.class, Damnation.class})
class DarkWingsBringYourDownfallTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking 5/5 Demon token")
    void attackCreatesDemonToken() {
        gd.playerCommandZones.get(player1.getId()).add(new DarkWingsBringYourDownfall());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent demon = findPermanents(player1, "Demon").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(demon.isTapped()).isTrue();
        assertThat(gameLogContains("tapped and attacking")).isTrue();
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Abandons at the end step after two creatures you control die")
    void abandonsAfterTwoControlledCreaturesDie() {
        DarkWingsBringYourDownfall scheme = new DarkWingsBringYourDownfall();
        gd.playerCommandZones.get(player1.getId()).add(scheme);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyCreaturesWithDamnation();
        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.faceDownCommandZoneCards).contains(scheme.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme);
    }

    @Test
    @DisplayName("Opponent creature deaths do not satisfy the abandonment condition")
    void ignoresOpponentCreatureDeaths() {
        DarkWingsBringYourDownfall scheme = new DarkWingsBringYourDownfall();
        gd.playerCommandZones.get(player1.getId()).add(scheme);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyCreaturesWithDamnation();
        advanceToEndStep();

        assertThat(gd.playerCommandZones.get(player1.getId())).contains(scheme);
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(scheme.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noDeathsDoNotTriggerAbandonment() {
        DarkWingsBringYourDownfall scheme = new DarkWingsBringYourDownfall();
        gd.playerCommandZones.get(player1.getId()).add(scheme);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(scheme.getId());
    }

    @Test
    void demonTokenDeathCountsTowardAbandonment() {
        DarkWingsBringYourDownfall scheme = new DarkWingsBringYourDownfall();
        gd.playerCommandZones.get(player1.getId()).add(scheme);
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);

        destroyCreaturesWithDamnation();
        advanceToEndStep();
        resolveAllTriggers();

        assertThat(gd.faceDownCommandZoneCards).contains(scheme.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme);
    }

    @Test
    void multipleAttackersCreateOnlyOneDemon() {
        gd.playerCommandZones.get(player1.getId()).add(new DarkWingsBringYourDownfall());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
    }

    @Test
    void opponentAttackDoesNotCreateDemon() {
        gd.playerCommandZones.get(player1.getId()).add(new DarkWingsBringYourDownfall());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Demon")).isZero();
        assertThat(countPermanents(player2, "Demon")).isZero();
    }

    @Test
    void abandonsOnOpponentEndStepAfterTwoControlledCreaturesDie() {
        DarkWingsBringYourDownfall scheme = new DarkWingsBringYourDownfall();
        gd.playerCommandZones.get(player1.getId()).add(scheme);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Damnation()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player2, 0, 0);

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.faceDownCommandZoneCards).contains(scheme.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme);
    }

    private void destroyCreaturesWithDamnation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Damnation()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void advanceToEndStep() {
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
