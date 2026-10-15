package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaverickThopterist.class, Ornithopter.class})
class MaverickThopteristTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 1/1 colorless Thopter artifact creature tokens with flying")
    void etbCreatesTwoThopters() {
        harness.setHand(player1, List.of(new MaverickThopterist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        });
    }

    @Test
    @DisplayName("Improvise pays all generic mana using summoning-sick artifacts")
    void improvisePaysGenericCostWithSummoningSickArtifacts() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        harness.setHand(player1, List.of(new MaverickThopterist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(first.getId(), second.getId(), third.getId()));

        assertThat(List.of(first, second, third)).allSatisfy(p -> assertThat(p.isTapped()).isTrue());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Maverick Thopterist");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(2);
    }

    @Test
    @DisplayName("Improvise cannot pay the blue mana requirement")
    void improviseCannotReplaceColoredMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new MaverickThopterist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(artifact.getId()))).isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Maverick Thopterist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Already tapped artifacts cannot help cast the spell")
    void tappedArtifactCannotImprovise() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.setHand(player1, List.of(new MaverickThopterist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(artifact.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast creates tokens for the entering creature's controller")
    void enteringWithoutCastingCreatesColorlessThopterCreaturesForController() {
        harness.enterBattlefieldAndReturn(player2, new MaverickThopterist());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
