package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.InkEyesServantOfOni;
import com.github.laxika.magicalvibes.cards.t.TeardropKami;
import com.github.laxika.magicalvibes.cards.t.ThatWhichWasTaken;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HerosDemise.class, InkEyesServantOfOni.class, TeardropKami.class, ThatWhichWasTaken.class})
class HerosDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Hero's Demise destroys the target legendary creature")
    void resolvingDestroysLegendaryCreature() {
        Permanent inkEyes = addCreatureReady(player2, new InkEyesServantOfOni());

        harness.setHand(player1, List.of(new HerosDemise()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, inkEyes.getId());

        harness.assertNotOnBattlefield(player2, "Ink-Eyes, Servant of Oni");
        harness.assertInGraveyard(player2, "Ink-Eyes, Servant of Oni");
        harness.assertInGraveyard(player1, "Hero's Demise");
    }

    @Test
    @DisplayName("Cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        addCreatureReady(player1, new InkEyesServantOfOni());

        Permanent kami = addCreatureReady(player2, new TeardropKami());

        harness.setHand(player1, List.of(new HerosDemise()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, kami.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Cannot target a legendary noncreature permanent")
    void cannotTargetLegendaryNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ThatWhichWasTaken());

        harness.setHand(player1, List.of(new HerosDemise()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }
}
