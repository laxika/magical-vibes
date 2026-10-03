package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.cards.c.CravenHulk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SpiritOfTheAldergard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlizzardBrawl.class, AirElemental.class, HillGiant.class,
        SnowCoveredForest.class, Forest.class, AxgardCavalry.class, CravenHulk.class,
        SpiritOfTheAldergard.class})
class BlizzardBrawlTest extends BaseCardTest {

    @Test
    @DisplayName("Three snow permanents grant the pump and indestructible before the fight")
    void threeSnowPermanentsGrantBonusBeforeFight() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID hillGiantId = harness.getPermanentId(player1, "Hill Giant");
        UUID airElementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveSorcery(player1, 0, List.of(hillGiantId, airElementalId));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Fewer than three snow permanents do not grant the bonus")
    void fewerThanThreeSnowPermanentsDoNotGrantBonus() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID hillGiantId = harness.getPermanentId(player1, "Hill Giant");
        UUID airElementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveSorcery(player1, 0, List.of(hillGiantId, airElementalId));

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("The first target must be a creature you control")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID airElementalId = harness.getPermanentId(player2, "Air Elemental");
        UUID hillGiantId = harness.getPermanentId(player1, "Hill Giant");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(airElementalId, hillGiantId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void cannotTargetOwnCreatureAsSecondTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CravenHulk());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bonusesApplyWithoutFightWhenOpponentTargetLeaves() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new SnowCoveredForest());
        }
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, own, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(own.getMarkedDamage()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, own, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void noFightWhenOwnTargetLeaves() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(own);
        harness.passBothPriorities();

        assertThat(opponent.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void snowCountIsCheckedAtResolutionRatherThanCasting() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Axgard Cavalry");
        assertThat(gqs.hasKeyword(gd, own, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opponent.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void losingThirdSnowPermanentBeforeResolutionRemovesBonuses() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        Permanent thirdSnow = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(thirdSnow);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Axgard Cavalry");
        assertThat(opponent.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void snowCreatureCountsTowardThreshold() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheAldergard());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponent.getId()));

        harness.assertOnBattlefield(player1, "Spirit of the Aldergard");
        assertThat(gqs.hasKeyword(gd, own, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opponent.getMarkedDamage()).isEqualTo(3);
    }
}
