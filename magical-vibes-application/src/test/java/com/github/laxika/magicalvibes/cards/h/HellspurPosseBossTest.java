package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellspurPosseBoss.class, DauthiMercenary.class, GrizzlyBears.class})
class HellspurPosseBossTest extends BaseCardTest {

    @Test
    @DisplayName("Other outlaws you control have haste")
    void grantsHasteToOtherOutlawsYouControl() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());
        Permanent boss = harness.addToBattlefieldAndReturn(player1, new HellspurPosseBoss());

        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, boss, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is not granted to non-outlaws or opposing outlaws")
    void restrictsHasteToOtherOutlawsYouControl() {
        Permanent opposingOutlaw = harness.addToBattlefieldAndReturn(player2, new DauthiMercenary());
        Permanent nonOutlaw = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HellspurPosseBoss());

        assertThat(gqs.hasKeyword(gd, opposingOutlaw, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonOutlaw, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Entering the battlefield creates two Mercenary tokens")
    void createsTwoMercenaryTokens() {
        castBoss();

        List<Permanent> mercenaries = findPermanents(player1, "Mercenary");
        assertThat(mercenaries).hasSize(2);
        assertThat(mercenaries.getFirst().getCard().isToken()).isTrue();
        assertThat(mercenaries.getLast().getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, mercenaries.getFirst(), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Mercenary tokens can boost a creature you control at sorcery speed")
    void mercenaryTokenBoostsCreatureYouControl() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castBoss();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mercenary tokens cannot activate their ability outside sorcery speed")
    void mercenaryTokenRequiresSorcerySpeed() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castBoss();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void castBoss() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HellspurPosseBoss()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
