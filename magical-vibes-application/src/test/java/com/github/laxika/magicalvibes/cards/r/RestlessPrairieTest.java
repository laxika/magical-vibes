package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({RestlessPrairie.class, GrizzlyBears.class})
class RestlessPrairieTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Prairie enters tapped and produces green or white mana")
    void entersTappedAndProducesMana() {
        harness.setHand(player1, List.of(new RestlessPrairie()));
        harness.playLand(player1, 0);

        Permanent prairie = findPermanent(player1, "Restless Prairie");
        assertThat(prairie.isTapped()).isTrue();

        prairie.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Restless Prairie becomes a 3/3 green and white Llama and stays a land")
    void animatesIntoRestlessPrairie() {
        Permanent prairie = addReadyPrairie(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, prairie)).isTrue();
        assertThat(gqs.isLand(gd, prairie)).isTrue();
        assertThat(gqs.getEffectivePower(gd, prairie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prairie)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, prairie))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    @DisplayName("When Restless Prairie attacks, other own creatures get +1/+1")
    void attackBoostsOtherOwnCreatures() {
        Permanent prairie = addReadyPrairie(player1);
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, prairie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prairie)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Restless Prairie's animation and attack boost end at end of turn")
    void effectsEndAtEndOfTurn() {
        Permanent prairie = addReadyPrairie(player1);
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, prairie)).isFalse();
        assertThat(gqs.isLand(gd, prairie)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    private void addAnimationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    private Permanent addReadyPrairie(Player player) {
        Permanent permanent = new Permanent(new RestlessPrairie());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
