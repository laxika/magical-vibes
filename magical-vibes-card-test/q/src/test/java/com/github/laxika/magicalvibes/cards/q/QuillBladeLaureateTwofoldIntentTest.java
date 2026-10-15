package com.github.laxika.magicalvibes.cards.q;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuillBladeLaureateTwofoldIntent.class, GrizzlyBears.class})
class QuillBladeLaureateTwofoldIntentTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prepares Quill-Blade Laureate and exiles a castable Twofold Intent copy")
    void entersPrepared() {
        Permanent laureate = castLaureate();

        assertThat(laureate.isPrepared()).isTrue();
        UUID copyId = laureate.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting Twofold Intent unprepares Quill-Blade Laureate and boosts the target")
    void castingPrepareCopyBoostsTarget() {
        Permanent laureate = castLaureate();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID copyId = laureate.getPreparedSpellCardId();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(laureate.isPrepared()).isFalse();
        assertThat(laureate.getPreparedSpellCardId()).isNull();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Twofold Intent's temporary effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent laureate = castLaureate();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID copyId = laureate.getPreparedSpellCardId();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Casting the prepare spell consumes preparation before the spell resolves")
    void castingConsumesPreparationImmediately() {
        Permanent laureate = castLaureate();
        UUID copyId = laureate.getPreparedSpellCardId();
        int originalPower = laureate.getEffectivePower();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castFromExile(player1, copyId, laureate.getId());

        assertThat(laureate.isPrepared()).isFalse();
        assertThat(laureate.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(laureate.getEffectivePower()).isEqualTo(originalPower);

        harness.passBothPriorities();

        assertThat(laureate.getEffectivePower()).isEqualTo(originalPower + 1);
    }

    @Test
    @DisplayName("The prepare sorcery cannot be cast outside a main phase and stays prepared")
    void prepareSpellRequiresSorceryTiming() {
        Permanent laureate = castLaureate();
        UUID copyId = laureate.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, laureate.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(laureate.isPrepared()).isTrue();
        assertThat(laureate.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The prepare spell requires its mana cost and a failed payment preserves preparation")
    void failedPaymentPreservesPreparation() {
        Permanent laureate = castLaureate();
        UUID copyId = laureate.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, laureate.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(laureate.isPrepared()).isTrue();
        assertThat(laureate.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castLaureate() {
        harness.setHand(player1, List.of(new QuillBladeLaureateTwofoldIntent()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return findPermanent(player1, "Quill-Blade Laureate");
    }
}
