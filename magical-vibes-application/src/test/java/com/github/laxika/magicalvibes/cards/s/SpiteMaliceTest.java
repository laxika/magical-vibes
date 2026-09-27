package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KavuRunner;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.u.UrborgSkeleton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiteMalice.class, Opt.class, KavuRunner.class, UrborgSkeleton.class})
class SpiteMaliceTest extends BaseCardTest {

    private static final int SPITE = 0;
    private static final int MALICE = 1;

    @Test
    @DisplayName("Spite counters a noncreature spell")
    void spiteCountersNoncreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuRunner());
        Opt opt = new Opt();
        harness.setHand(player2, List.of(opt));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.setHand(player1, List.of(new SpiteMalice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, kavu.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, SPITE, opt.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Opt");
    }

    @Test
    @DisplayName("Spite cannot target a creature spell")
    void spiteCannotTargetCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        KavuRunner kavu = new KavuRunner();
        harness.setHand(player2, List.of(kavu));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.setHand(player1, List.of(new SpiteMalice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, SPITE, kavu.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Malice destroys a nonblack creature without allowing regeneration")
    void maliceDestroysNonblackCreatureWithoutRegeneration() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuRunner());
        kavu.setRegenerationShield(1);

        harness.setHand(player1, List.of(new SpiteMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, MALICE, kavu.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kavu Runner");
        harness.assertInGraveyard(player2, "Kavu Runner");
    }

    @Test
    @DisplayName("Malice cannot target a black creature")
    void maliceCannotTargetBlackCreature() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player2, new UrborgSkeleton());

        harness.setHand(player1, List.of(new SpiteMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, MALICE, skeleton.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spite cannot target an activated ability")
    void spiteCannotTargetActivatedAbility() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player2, new UrborgSkeleton());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SpiteMalice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, SPITE, skeleton.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
