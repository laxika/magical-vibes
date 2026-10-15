package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyleaGodOfTheHunt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HerosDownfall.class, GrizzlyBears.class, GarrukWildspeaker.class, Forest.class, NyleaGodOfTheHunt.class})
class HerosDownfallTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature")
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a target planeswalker")
    void destroysTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 5);
        cast(target);

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Rejects a noncreature nonplaneswalker target")
    void rejectsNoncreatureNonplaneswalkerTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HerosDownfall()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy a creature controlled by its caster")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects a God that is not currently a creature")
    void rejectsNoncreatureGod() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyleaGodOfTheHunt());
        harness.setHand(player1, List.of(new HerosDownfall()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Nylea, God of the Hunt");
    }

    @Test
    @DisplayName("An indestructible God with enough devotion is a legal target but survives")
    void indestructibleCreatureSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyleaGodOfTheHunt());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        cast(target);

        harness.assertOnBattlefield(player2, "Nylea, God of the Hunt");
        harness.assertNotInGraveyard(player2, "Nylea, God of the Hunt");
        harness.assertInGraveyard(player1, "Hero's Downfall");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new HerosDownfall()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
