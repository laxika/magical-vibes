package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({RestlessVents.class, Forest.class, GrizzlyBears.class})
class RestlessVentsTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Vents enters tapped and adds black or red mana")
    void entersTappedAndProducesChosenMana() {
        harness.setHand(player1, List.of(new RestlessVents()));
        harness.playLand(player1, 0);

        Permanent vents = findPermanent(player1, "Restless Vents");
        assertThat(vents.isTapped()).isTrue();

        vents.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Restless Vents becomes a 2/3 black and red Insect with menace")
    void animatesIntoMenacingInsect() {
        Permanent vents = addReadyVents(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vents)).isTrue();
        assertThat(gqs.isLand(gd, vents)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vents)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vents)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, vents))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(gqs.effectiveCreatureSubtypes(gd, vents)).contains(CardSubtype.INSECT);
        assertThat(gqs.hasKeyword(gd, vents, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Restless Vents's animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent vents = addReadyVents(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vents)).isFalse();
        assertThat(gqs.isLand(gd, vents)).isTrue();
        assertThat(gqs.hasKeyword(gd, vents, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Attacking with Restless Vents lets its controller discard and draw")
    void attackingDiscardsThenDraws() {
        Permanent vents = addReadyVents(player1);
        addAnimationMana(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GrizzlyBears discarded = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining Restless Vents's attack trigger does not discard or draw")
    void decliningAttackTriggerDoesNothing() {
        Permanent vents = addReadyVents(player1);
        addAnimationMana(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(retained)));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void addAnimationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
    }

    private Permanent addReadyVents(Player player) {
        Permanent permanent = new Permanent(new RestlessVents());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
