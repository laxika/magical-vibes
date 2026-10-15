package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SaprolingMigration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifeAndLimb.class, Forest.class, Mountain.class, SaprolingMigration.class})
class LifeAndLimbTest extends BaseCardTest {

    @Test
    @DisplayName("Forests and Saprolings become green 1/1 creature lands")
    void animatesForestsAndSaprolings() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new LifeAndLimb());

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.SAPROLING);

        assertThat(gqs.isCreature(gd, opponentForest)).isTrue();
        assertThat(gqs.isLand(gd, opponentForest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentForest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentForest)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, opponentForest)).containsExactly(CardColor.GREEN);

        assertThat(gqs.isCreature(gd, mountain)).isFalse();
    }

    @Test
    @DisplayName("Saprolings gain the Forest mana ability")
    void saprolingsTapForGreenMana() {
        harness.castFromHand(player1, new SaprolingMigration(), "{1}{G}");
        harness.passBothPriorities();

        Permanent saproling = findPermanent(player1, "Saproling");
        saproling.setSummoningSick(false);
        harness.addToBattlefield(player1, new LifeAndLimb());

        assertThat(gqs.isCreature(gd, saproling)).isTrue();
        assertThat(gqs.isLand(gd, saproling)).isTrue();
        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, saproling)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasEffectiveSubtype(gd, saproling, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, saproling)).contains(CardSubtype.SAPROLING);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(saproling),
                0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Permanents entering after Life and Limb are animated continuously")
    void animatesPermanentsEnteringAfterward() {
        harness.addToBattlefield(player1, new LifeAndLimb());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.SAPROLING)).isTrue();

        harness.castFromHand(player1, new SaprolingMigration(), "{1}{G}");
        harness.passBothPriorities();

        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(gqs.isCreature(gd, saproling)).isTrue();
        assertThat(gqs.isLand(gd, saproling)).isTrue();
        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, saproling)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasEffectiveSubtype(gd, saproling, CardSubtype.FOREST)).isTrue();
    }

    @Test
    @DisplayName("Newly created Saprolings cannot use the granted mana ability")
    void newlyCreatedSaprolingsHaveSummoningSickness() {
        harness.addToBattlefield(player1, new LifeAndLimb());
        harness.castFromHand(player1, new SaprolingMigration(), "{1}{G}");
        harness.passBothPriorities();

        Permanent saproling = findPermanent(player1, "Saproling");
        int saprolingIndex = gd.playerBattlefields.get(player1.getId()).indexOf(saproling);

        assertThatThrownBy(() -> harness.activateAbility(player1, saprolingIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(saproling.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Forest played under Life and Limb cannot tap for mana immediately")
    void newlyPlayedForestHasSummoningSickness() {
        harness.addToBattlefield(player1, new LifeAndLimb());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, forestIndex))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("An established Forest can still tap for mana after being animated")
    void establishedForestCanTapForMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setSummoningSick(false);
        harness.addToBattlefield(player1, new LifeAndLimb());

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Forests and Saprolings revert when Life and Limb leaves the battlefield")
    void animationEndsWhenSourceLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new SaprolingMigration(), "{1}{G}");
        harness.passBothPriorities();
        Permanent saproling = findPermanent(player1, "Saproling");
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new LifeAndLimb());

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, saproling)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.SAPROLING)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();
        assertThat(gqs.isCreature(gd, saproling)).isTrue();
        assertThat(gqs.isLand(gd, saproling)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, saproling, CardSubtype.FOREST)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, saproling, CardSubtype.SAPROLING)).isTrue();
    }
}
