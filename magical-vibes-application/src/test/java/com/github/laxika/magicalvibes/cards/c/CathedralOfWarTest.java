package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CathedralOfWar.class, ElvishArchdruid.class})
class CathedralOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new CathedralOfWar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Cathedral of War").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps for one colorless mana")
    void tapsForColorless() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CathedralOfWar());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void tappedCathedralBoostsLoneAttackerUntilEndOfTurn() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CathedralOfWar());
        land.tap();
        Permanent attacker = addCreatureReady(player1, new ElvishArchdruid());
        int power = gqs.getEffectivePower(gd, attacker);
        int toughness = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughness + 1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughness + 1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughness);
    }

    @Test
    void multipleCathedralsEachBoostLoneAttacker() {
        harness.addToBattlefield(player1, new CathedralOfWar());
        harness.addToBattlefield(player1, new CathedralOfWar());
        Permanent attacker = addCreatureReady(player1, new ElvishArchdruid());
        int power = gqs.getEffectivePower(gd, attacker);
        int toughness = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughness + 2);
    }

    @Test
    void doesNotBoostMultipleAttackers() {
        harness.addToBattlefield(player1, new CathedralOfWar());
        Permanent first = addCreatureReady(player1, new ElvishArchdruid());
        Permanent second = addCreatureReady(player1, new ElvishArchdruid());
        int power = gqs.getEffectivePower(gd, first);
        int toughness = gqs.getEffectiveToughness(gd, first);

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(toughness);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(toughness);
    }

    @Test
    void doesNotBoostOpponentsLoneAttacker() {
        harness.addToBattlefield(player1, new CathedralOfWar());
        Permanent attacker = addCreatureReady(player2, new ElvishArchdruid());
        int power = gqs.getEffectivePower(gd, attacker);
        int toughness = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughness);
    }
}
