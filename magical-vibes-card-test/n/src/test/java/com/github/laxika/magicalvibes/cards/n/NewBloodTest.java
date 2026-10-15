package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LordOfAtlantis;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NewBlood.class, BaronyVampire.class, LordOfAtlantis.class, GrizzlyBears.class, Mountain.class})
class NewBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a Vampire, permanently gains control, and changes the target's creature type text")
    void tapsVampireGainsControlAndChangesText() {
        Permanent vampire = addCreatureReady(player1, new BaronyVampire());
        Permanent lord = addCreatureReady(player2, new LordOfAtlantis());

        harness.setHand(player1, List.of(new NewBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryTappingPermanents(player1, 0, lord.getId(), List.of(vampire.getId()));
        assertThat(vampire.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "MERFOLK");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lord);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(lord);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(lord.getTextReplacements()).containsExactly(new TextReplacement("Merfolk", "Vampire"));
        assertThat(gqs.effectiveCreatureSubtypes(gd, lord)).containsExactly(CardSubtype.VAMPIRE);
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    @DisplayName("Requires an untapped Vampire to pay the additional cost")
    void rejectsNonVampireAdditionalCost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LordOfAtlantis());

        harness.setHand(player1, List.of(new NewBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, target.getId(), List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature")
    void rejectsNonCreatureTarget() {
        Permanent vampire = addCreatureReady(player1, new BaronyVampire());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.setHand(player1, List.of(new NewBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, land.getId(), List.of(vampire.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vampire.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the additional cost with an already tapped Vampire")
    void rejectsTappedVampireAdditionalCost() {
        Permanent vampire = addCreatureReady(player1, new BaronyVampire());
        vampire.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NewBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, target.getId(), List.of(vampire.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vampire.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot pay the additional cost with an opponent's Vampire")
    void rejectsOpponentsVampireAdditionalCost() {
        addCreatureReady(player1, new BaronyVampire());
        Permanent vampire = addCreatureReady(player2, new BaronyVampire());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NewBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, target.getId(), List.of(vampire.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vampire.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Vampire can pay the additional cost")
    void canTapSummoningSickVampire() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BaronyVampire());
        vampire.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NewBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(vampire.getId()));
        assertThat(vampire.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Can choose Vampire itself as the type to replace")
    void canChooseVampireAsOriginalType() {
        Permanent vampire = addCreatureReady(player1, new BaronyVampire());
        Permanent target = addCreatureReady(player2, new BaronyVampire());
        harness.setHand(player1, List.of(new NewBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(vampire.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "VAMPIRE");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Choosing a type absent from the creature does not change its types")
    void canChooseAbsentCreatureType() {
        Permanent vampire = addCreatureReady(player1, new BaronyVampire());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NewBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(vampire.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "DRAGON");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.BEAR);
    }
}
