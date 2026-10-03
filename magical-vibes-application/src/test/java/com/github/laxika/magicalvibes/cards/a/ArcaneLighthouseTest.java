package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.c.CrystallineSliver;
import com.github.laxika.magicalvibes.cards.h.HorrorOfTheDim;
import com.github.laxika.magicalvibes.cards.k.KnightOfGrace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.ViciousOffering;
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

@CardUsed({ArcaneLighthouse.class, CarnageTyrant.class, CrystallineSliver.class,
        HorrorOfTheDim.class, Shock.class, KnightOfGrace.class, ViciousOffering.class})
class ArcaneLighthouseTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one colorless mana")
    void tappingAddsColorlessMana() {
        Permanent lighthouse = harness.addToBattlefieldAndReturn(player1, new ArcaneLighthouse());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(lighthouse.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removes opposing hexproof and shroud and permits targeting until end of turn")
    void removesOpposingHexproofAndShroudUntilEndOfTurn() {
        Permanent lighthouse = harness.addToBattlefieldAndReturn(player1, new ArcaneLighthouse());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CarnageTyrant());
        Permanent opponentHexproof = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        Permanent opponentShroud = harness.addToBattlefieldAndReturn(player2, new CrystallineSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentShroud, Keyword.SHROUD)).isTrue();

        int lighthouseIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lighthouse);
        harness.activateAbility(player1, lighthouseIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentShroud, Keyword.SHROUD)).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, opponentHexproof.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentShroud, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Prevents opposing creatures from gaining hexproof during the turn")
    void preventsOpposingCreaturesFromGainingHexproof() {
        Permanent lighthouse = harness.addToBattlefieldAndReturn(player1, new ArcaneLighthouse());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HorrorOfTheDim());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lighthouseIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lighthouse);
        harness.activateAbility(player1, lighthouseIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.withFreshQueryScope(gd,
                () -> gqs.cantHaveOrGainKeyword(gd, opponent, Keyword.HEXPROOF))).isTrue();

        harness.addMana(player2, ManaColor.BLUE, 1);
        int opponentIndex = gd.playerBattlefields.get(player2.getId()).indexOf(opponent);
        harness.activateAbility(player2, opponentIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Only creatures present at resolution lose hexproof and shroud")
    void laterCreaturesRetainHexproofAndShroud() {
        harness.addToBattlefield(player1, new ArcaneLighthouse());
        Permanent existingHexproof = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        Permanent existingShroud = harness.addToBattlefieldAndReturn(player2, new CrystallineSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gqs.hasKeyword(gd, existingHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, existingShroud, Keyword.SHROUD)).isTrue();
        harness.passBothPriorities();

        Permanent laterHexproof = harness.enterBattlefieldAndReturn(player2, new CarnageTyrant());
        Permanent laterShroud = harness.enterBattlefieldAndReturn(player2, new CrystallineSliver());

        assertThat(gqs.hasKeyword(gd, existingHexproof, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, existingShroud, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterShroud, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Removing the Lighthouse does not end its resolved ability")
    void resolvedAbilityPersistsWithoutLighthouse() {
        Permanent lighthouse = harness.addToBattlefieldAndReturn(player1, new ArcaneLighthouse());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HorrorOfTheDim());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, lighthouse));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Losing hexproof also removes hexproof from black")
    void removesHexproofFromBlack() {
        harness.addToBattlefield(player1, new ArcaneLighthouse());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KnightOfGrace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, opponent.getId());

        harness.assertInGraveyard(player2, "Knight of Grace");
    }
}
