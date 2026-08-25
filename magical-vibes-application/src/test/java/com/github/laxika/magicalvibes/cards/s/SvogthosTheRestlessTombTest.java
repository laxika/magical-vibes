package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SvogthosTheRestlessTomb.class, GrizzlyBears.class, Forest.class})
class SvogthosTheRestlessTombTest extends BaseCardTest {

    @Test
    @DisplayName("Svogthos becomes a black and green Plant Zombie with power and toughness equal to creature cards in its controller's graveyard")
    void activatesAsGraveyardSizedCreature() {
        Permanent svogthos = addSvogthosReady(player1);
        List<Card> graveyard = new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player1, graveyard);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        activate(svogthos);

        assertThat(gqs.isCreature(gd, svogthos)).isTrue();
        assertThat(gqs.isLand(gd, svogthos)).isTrue();
        assertThat(gqs.getEffectivePower(gd, svogthos)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, svogthos)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, svogthos))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(svogthos.getTransientSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PLANT, CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Svogthos continuously tracks creature cards entering and leaving its controller's graveyard")
    void tracksGraveyardChangesDuringAnimation() {
        Permanent svogthos = addSvogthosReady(player1);
        List<Card> graveyard = new ArrayList<>(List.of(new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player1, graveyard);
        activate(svogthos);

        assertThat(gqs.getEffectivePower(gd, svogthos)).isEqualTo(1);
        graveyard = gd.playerGraveyards.get(player1.getId());
        graveyard.add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, svogthos)).isEqualTo(2);

        graveyard.removeIf(card -> card instanceof GrizzlyBears);
        graveyard.add(new GrizzlyBears());
        assertThat(gqs.getEffectiveToughness(gd, svogthos)).isEqualTo(1);
    }

    @Test
    @DisplayName("Svogthos stops being animated at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent svogthos = addSvogthosReady(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        activate(svogthos);

        assertThat(gqs.isCreature(gd, svogthos)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, svogthos)).isFalse();
        assertThat(svogthos.getTransientSubtypes()).isEmpty();
    }

    private void activate(Permanent svogthos) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addSvogthosReady(Player player) {
        Permanent permanent = new Permanent(new SvogthosTheRestlessTomb());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
