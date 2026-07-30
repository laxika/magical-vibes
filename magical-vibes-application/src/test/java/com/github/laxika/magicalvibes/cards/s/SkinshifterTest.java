package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkinshifterTest extends BaseCardTest {

    private static final String RHINO_MODE =
            "Until end of turn, this creature becomes a Rhino with base power and toughness 4/4 and gains trample.";
    private static final String BIRD_MODE =
            "Until end of turn, this creature becomes a Bird with base power and toughness 2/2 and gains flying.";
    private static final String PLANT_MODE =
            "Until end of turn, this creature becomes a Plant with base power and toughness 0/8.";

    @Test
    @DisplayName("Rhino mode makes it a 4/4 Rhino with trample")
    void rhinoMode() {
        Permanent skinshifter = addSkinshifter(player1);

        activate(player1);
        harness.handleListChoice(player1, RHINO_MODE);

        assertThat(skinshifter.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.RHINO);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Bird mode makes it a 2/2 Bird with flying")
    void birdMode() {
        Permanent skinshifter = addSkinshifter(player1);

        activate(player1);
        harness.handleListChoice(player1, BIRD_MODE);

        assertThat(skinshifter.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.BIRD);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Plant mode makes it a 0/8 Plant with no granted keyword")
    void plantMode() {
        Permanent skinshifter = addSkinshifter(player1);

        activate(player1);
        harness.handleListChoice(player1, PLANT_MODE);

        assertThat(skinshifter.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.PLANT);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The type change, base P/T and keyword wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent skinshifter = addSkinshifter(player1);

        activate(player1);
        harness.handleListChoice(player1, RHINO_MODE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skinshifter.getTransientCreatureTypeOverride()).isNull();
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void onlyOnceEachTurn() {
        addSkinshifter(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        activate(player1);
        harness.handleListChoice(player1, BIRD_MODE);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unknown mode label is rejected")
    void illegalModeRejected() {
        addSkinshifter(player1);

        activate(player1);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Until end of turn, this creature becomes a Wurm."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Permanent addSkinshifter(Player player) {
        Permanent permanent = new Permanent(new Skinshifter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    /** Pays {G}, activates the ability and resolves it up to the mode prompt. */
    private void activate(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.activateAbility(player, 0, null, null);
        harness.passBothPriorities();
    }
}
