package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgonizingDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature without kicker")
    void destroysCreatureWithoutKicker() {
        Permanent bears = addTarget(new GrizzlyBears());
        cast(bears, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Destroys the creature and deals damage equal to its power when kicked")
    void destroysCreatureAndDamagesItsControllerWhenKicked() {
        Permanent bears = addTarget(new GrizzlyBears());
        cast(bears, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The destruction cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent bears = addTarget(new GrizzlyBears());
        bears.setRegenerationShield(1);
        cast(bears, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = addTarget(new MassOfGhouls());
        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTarget(Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player2.getId()).add(permanent);
        return permanent;
    }

    private void cast(Permanent target, boolean kicked) {
        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        if (kicked) {
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
        }

        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
        harness.passBothPriorities();
    }
}
