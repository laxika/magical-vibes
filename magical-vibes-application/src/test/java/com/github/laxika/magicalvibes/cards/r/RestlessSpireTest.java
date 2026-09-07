package com.github.laxika.magicalvibes.cards.r;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RestlessSpire.class})
class RestlessSpireTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Spire enters tapped and produces blue or red mana")
    void entersTappedAndProducesMana() {
        harness.setHand(player1, List.of(new RestlessSpire()));
        harness.playLand(player1, 0);

        Permanent spire = findPermanent(player1, "Restless Spire");
        assertThat(spire.isTapped()).isTrue();

        spire.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Restless Spire becomes a 2/1 blue and red Elemental and has first strike during its controller's turn")
    void animatesIntoElemental() {
        Permanent spire = addReadySpire(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spire)).isTrue();
        assertThat(gqs.isLand(gd, spire)).isTrue();
        assertThat(gqs.getEffectivePower(gd, spire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spire)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, spire))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(gqs.effectiveCreatureSubtypes(gd, spire)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, spire, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, spire, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Attacking with Restless Spire triggers scry 1")
    void attackingTriggersScry() {
        addReadySpire(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Restless Spire's animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        Permanent spire = addReadySpire(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spire)).isFalse();
        assertThat(gqs.isLand(gd, spire)).isTrue();
    }

    private Permanent addReadySpire(Player player) {
        Permanent permanent = new Permanent(new RestlessSpire());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
