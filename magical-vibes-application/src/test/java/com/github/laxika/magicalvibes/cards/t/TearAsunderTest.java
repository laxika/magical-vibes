package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TearAsunder.class, AngelicChorus.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class TearAsunderTest extends BaseCardTest {

    @Test
    void withoutKickerExilesAnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        cast(false, target);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void withoutKickerExilesAnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        cast(false, target);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void withoutKickerRejectsACreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TearAsunder()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    void kickedExilesANonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(true, target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void kickedRejectsALandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TearAsunder()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    private void cast(boolean kicked, Permanent target) {
        harness.setHand(player1, List.of(new TearAsunder()));
        if (kicked) {
            addKickedMana();
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            addBaseMana();
            harness.castInstant(player1, 0, target.getId());
        }
        harness.passBothPriorities();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
