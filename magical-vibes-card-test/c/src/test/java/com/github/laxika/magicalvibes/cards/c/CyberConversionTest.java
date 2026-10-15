package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AutonSoldier;
import com.github.laxika.magicalvibes.cards.h.HuntmasterOfTheFells;
import com.github.laxika.magicalvibes.cards.t.TheFugitiveDoctor;
import com.github.laxika.magicalvibes.cards.w.WeepingAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CyberConversion.class, Forest.class, TheFugitiveDoctor.class,
        WeepingAngel.class, AutonSoldier.class, HuntmasterOfTheFells.class})
class CyberConversionTest extends BaseCardTest {

    @Test
    void turnsTargetCreatureFaceDownAsCyberman() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheFugitiveDoctor());

        castCyberConversion(target);

        assertThat(target.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardTypes(gd, target))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .containsExactly(CardSubtype.CYBERMAN);
    }

    @Test
    void turningThePermanentFaceUpRestoresItsOriginalCharacteristics() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheFugitiveDoctor());
        castCyberConversion(target);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, target);
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        assertThat(gqs.getEffectiveCardTypes(gd, target)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .containsExactlyInAnyOrder(CardSubtype.TIME_LORD, CardSubtype.DOCTOR);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CyberConversion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castCyberConversion(Permanent target) {
        harness.setHand(player1, List.of(new CyberConversion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    void removesNameColorsAndPrintedKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WeepingAngel());

        castCyberConversion(target);

        assertThat(gqs.getEffectiveName(gd, target)).isNull();
        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void leavesAlreadyFaceDownCreatureUnchanged() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheFugitiveDoctor());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        castCyberConversion(target);

        assertThat(target.isFaceDown()).isTrue();
        assertThat(gqs.getEffectiveCardTypes(gd, target)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).isEmpty();
    }

    @Test
    void cannotTurnDoubleFacedCreatureFaceDown() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HuntmasterOfTheFells());

        castCyberConversion(target);

        assertThat(target.isFaceDown()).isFalse();
        assertThat(gqs.getEffectiveCardTypes(gd, target)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.WEREWOLF);
    }

    @Test
    void turningCopyFaceUpPreservesItsCopyEffect() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new TheFugitiveDoctor());
        harness.castFromHand(player1, new AutonSoldier(), "{4}{U}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        resolveAllTriggers();
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getCard().isToken())
                .findFirst().orElseThrow();

        castCyberConversion(copy);
        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, copy);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, copy.getId())).isNotNull();
        assertThat(gqs.getEffectiveName(gd, copy)).isEqualTo("The Fugitive Doctor");
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(4);
    }
}
