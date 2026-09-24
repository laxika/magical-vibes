package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElmarUlvenwaldInformant.class, Shock.class, GrizzlyBears.class, Forest.class})
class ElmarUlvenwaldInformantTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell untaps a target creature and creates a Clue")
    void secondSpellUntapsTargetCreatureAndInvestigates() {
        addCreatureReady(player1, new ElmarUlvenwaldInformant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The trigger cannot target a noncreature permanent")
    void triggerCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new ElmarUlvenwaldInformant());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }
}
