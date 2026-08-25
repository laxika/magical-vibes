package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToxicStench.class, GrizzlyBears.class, WalkingCorpse.class, FountainOfYouth.class, Shock.class})
class ToxicStenchTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target nonblack creature -1/-1 without threshold")
    void givesMinusOneMinusOneWithoutThreshold() {
        Permanent target = addCreature();

        cast(target);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys the target without regeneration with threshold")
    void destroysTargetWithoutRegenerationWithThreshold() {
        Permanent target = addCreature();
        target.setRegenerationShield(1);
        setGraveyardSize(7);

        cast(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Checks threshold as the spell resolves")
    void checksThresholdAtResolution() {
        Permanent target = addCreature();
        setGraveyardSize(6);

        cast(target);
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new ToxicStench()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ToxicStench()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new ToxicStench()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
    }

    private void setGraveyardSize(int size) {
        harness.setGraveyard(player1, List.<Card>of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock()
        ).subList(0, size));
    }
}
