package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KaitoDancingShadow;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChargeOfTheMites.class, GrizzlyBears.class, KaitoDancingShadow.class})
class ChargeOfTheMitesTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals damage equal to the number of creatures controlled")
    void damageModeCountsControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Token mode creates two Mites that can't block")
    void tokenModeCreatesMitesThatCantBlock() {
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        List<Permanent> mites = findPermanents(player1, "Mite");
        assertThat(mites).hasSize(2);
        assertThat(mites).allSatisfy(mite -> assertThat(bls.canBlock(gd, mite)).isFalse());
    }

    @Test
    @DisplayName("Damage mode cannot target a player")
    void damageModeCannotTargetPlayer() {
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeCountsCreaturesAtResolutionIncludingTokens() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChargeOfTheMites(), new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mite")).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void damageModeDealsZeroWithoutControlledCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Charge of the Mites");
    }

    @Test
    void damageModeRemovesPlaneswalkerLoyalty() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new KaitoDancingShadow());
        harness.setHand(player1, List.of(new ChargeOfTheMites(), new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Kaito, Dancing Shadow");
    }

    @Test
    void tokenModeCreatesColorlessPhyrexianArtifactCreatures() {
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mite")).hasSize(2).allSatisfy(mite -> {
            assertThat(mite.getCard().isToken()).isTrue();
            assertThat(mite.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(mite.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(mite.getCard().getColor()).isNull();
            assertThat(mite.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.MITE);
            assertThat(gqs.getEffectivePower(gd, mite)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, mite)).isEqualTo(1);
        });
    }

    @Test
    void miteToxicGivesPoisonWithCombatDamageWithoutUsingStack() {
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        for (Permanent mite : findPermanents(player1, "Mite")) {
            mite.setSummoningSick(false);
            mite.setAttacking(true);
        }
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
