package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamDevourer.class, GrizzlyBears.class, DoomskarOracle.class,
        SnowCoveredForest.class, AncestralVision.class})
class DreamDevourerTest extends BaseCardTest {

    @BeforeEach
    void clearOpponentsHand() {
        harness.setHand(player2, List.of());
    }

    @Test
    void grantsForetellWithReducedCostAndBoostsWhenCardIsForetold() {
        Permanent dreamDevourer = addCreatureReady(player1, new DreamDevourer());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);
        resolveAllTriggers();

        ExiledCardEntry entry = gd.findExiledCard(bears.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, dreamDevourer)).isEqualTo(2);

        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromExile(player1, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void canForetellNonlandCardWithNoManaCost() {
        Permanent devourer = addCreatureReady(player1, new DreamDevourer());
        AncestralVision vision = new AncestralVision();
        harness.setHand(player1, List.of(vision));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(vision.getId())).isNotNull();
        assertThat(gd.findExiledCard(vision.getId()).faceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, devourer)).isEqualTo(2);
    }

    @Test
    void nativeForetellTriggersEachDevourerAndBonusesExpire() {
        Permanent first = addCreatureReady(player1, new DreamDevourer());
        Permanent second = addCreatureReady(player1, new DreamDevourer());
        DoomskarOracle oracle = new DoomskarOracle();
        harness.setHand(player1, List.of(oracle, new DoomskarOracle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.foretell(player1, 0);
        resolveAllTriggers();
        harness.foretell(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, first)).isZero();
        assertThat(gqs.getEffectivePower(gd, second)).isZero();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, oracle.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Doomskar Oracle");
        assertThat(gqs.getEffectivePower(gd, first)).isZero();
    }

    @Test
    void doesNotGrantForetellToLandsOrOpponentsCards() {
        addCreatureReady(player1, new DreamDevourer());
        harness.setHand(player1, List.of(new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("does not have foretell");

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DreamDevourer()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.foretell(player2, 0))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("does not have foretell");
    }

    @Test
    void grantedCostSurvivesSourceLeavingAndCannotBeUsedOnSameTurn() {
        Permanent devourer = addCreatureReady(player1, new DreamDevourer());
        DreamDevourer card = new DreamDevourer();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        gd.playerBattlefields.get(player1.getId()).remove(devourer);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, card.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Dream Devourer");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void opponentsForetellDoesNotBoostDevourer() {
        Permanent devourer = addCreatureReady(player1, new DreamDevourer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DoomskarOracle()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.foretell(player2, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, devourer)).isZero();
    }
}
