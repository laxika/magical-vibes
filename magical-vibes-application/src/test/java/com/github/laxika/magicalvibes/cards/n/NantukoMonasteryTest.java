package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NantukoMonastery.class, GrizzlyBears.class})
class NantukoMonasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Nantuko Monastery produces one colorless mana")
    void tapForColorless() {
        addMonasteryReady(player1);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The animation ability cannot be activated without threshold")
    void animationRequiresThreshold() {
        addMonasteryReady(player1);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Threshold animates Nantuko Monastery as a 4/4 green-white Insect Monk with first strike")
    void thresholdAnimatesMonastery() {
        Permanent monastery = addMonasteryReady(player1);
        setThresholdGraveyard(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monastery)).isTrue();
        assertThat(gqs.isLand(gd, monastery)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monastery)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, monastery)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, monastery))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(monastery.getTransientSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.INSECT, CardSubtype.MONK);
        assertThat(monastery.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("The animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        Permanent monastery = addMonasteryReady(player1);
        setThresholdGraveyard(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, monastery)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monastery)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, monastery)).isEmpty();
        assertThat(monastery.getTransientSubtypes()).isEmpty();
        assertThat(monastery.getGrantedKeywords()).isEmpty();
    }

    private Permanent addMonasteryReady(Player player) {
        NantukoMonastery monastery = new NantukoMonastery();
        Permanent permanent = new Permanent(monastery);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void setThresholdGraveyard(Player player) {
        harness.setGraveyard(player, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
