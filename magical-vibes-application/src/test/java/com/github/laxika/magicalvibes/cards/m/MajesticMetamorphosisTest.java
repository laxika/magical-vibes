package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.j.Jackhammer;
import com.github.laxika.magicalvibes.cards.j.JetmirsFixer;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MajesticMetamorphosis.class, Jackhammer.class, JetmirsFixer.class, Island.class, Murder.class})
class MajesticMetamorphosisTest extends BaseCardTest {

    @Test
    void transformsCreatureAndDrawsACardUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new JetmirsFixer());
        harness.setHand(player1, List.of(new MajesticMetamorphosis()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(target, CardSubtype.ANGEL)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(target, CardSubtype.ANGEL)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void transformsArtifactAndKeepsItAnArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Jackhammer());
        harness.setHand(player1, List.of(new MajesticMetamorphosis()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void cannotTargetAPermanentThatIsNeitherAnArtifactNorACreature() {
        Permanent invalidTarget = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new MajesticMetamorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, invalidTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    void replacesCreatureTypesWithAngelUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new JetmirsFixer());
        harness.setHand(player1, List.of(new MajesticMetamorphosis()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.ANGEL);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .containsExactlyInAnyOrder(CardSubtype.CAT, CardSubtype.WARRIOR);
    }

    @Test
    void retainsEquipmentSubtypeAndRevertsNoncreatureArtifactAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Jackhammer());
        harness.setHand(player1, List.of(new MajesticMetamorphosis()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(GameQueryService.permanentHasSubtype(target, CardSubtype.EQUIPMENT)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(target, CardSubtype.EQUIPMENT)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(target, CardSubtype.ANGEL)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new JetmirsFixer());
        harness.setHand(player1, List.of(new MajesticMetamorphosis()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Jetmir's Fixer");
        harness.assertInGraveyard(player1, "Majestic Metamorphosis");
    }
}
