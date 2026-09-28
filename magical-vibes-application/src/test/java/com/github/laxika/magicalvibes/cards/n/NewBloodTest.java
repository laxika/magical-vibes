package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LordOfAtlantis;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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
        harness.handleListChoice(player1, "VAMPIRE");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lord);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(lord);
        assertThat(lord.getTextReplacements()).containsExactly(new TextReplacement("Merfolk", "Vampire"));
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
}
