package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HyenaPack;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
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

@CardUsed({AhnCropCrasher.class, HyenaPack.class, LayClaim.class})
class AhnCropCrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Exert is chosen while declaring attackers, before selecting a trigger target")
    void exertChoicePrecedesTargetSelection() {
        addReadyCrasher(player1);
        addCreatureReady(player2, new HyenaPack());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exerting makes the target creature unable to block and skips the crasher's next untap")
    void exertMakesTargetUnableToBlock() {
        Permanent crasher = addReadyCrasher(player1);
        Permanent bears = addCreatureReady(player2, new HyenaPack());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isCantBlockThisTurn()).isTrue();
        assertThat(crasher.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves the target able to block and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent crasher = addReadyCrasher(player1);
        Permanent bears = addCreatureReady(player2, new HyenaPack());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isCantBlockThisTurn()).isFalse();
        assertThat(crasher.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert skips only the next untap step")
    void exertSkipsExactlyOneUntapStep() {
        Permanent crasher = addReadyCrasher(player1);
        Permanent target = addCreatureReady(player2, new HyenaPack());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(crasher.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(crasher.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An exerted creature can untap during a different controller's untap step")
    void exertDoesNotSkipNewControllersUntapStep() {
        Permanent crasher = addReadyCrasher(player1);
        Permanent target = addCreatureReady(player2, new HyenaPack());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LayClaim()));
        harness.addMana(player2, ManaColor.BLUE, 7);
        harness.castEnchantment(player2, 0, crasher.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(crasher);
        harness.performUntapStep(player2);

        assertThat(crasher.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exerting puts only the targeted reflexive ability on the stack")
    void exertPutsSingleReflexiveAbilityOnStack() {
        Permanent crasher = addReadyCrasher(player1);
        Permanent target = addCreatureReady(player2, new HyenaPack());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(crasher.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getTargetId()).isEqualTo(target.getId()));
    }

    private Permanent addReadyCrasher(Player player) {
        return addCreatureReady(player, new AhnCropCrasher());
    }
}
