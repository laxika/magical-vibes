package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AvacynAngelOfHope;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturalEnd.class, AngelicChorus.class, FountainOfYouth.class, GrizzlyBears.class,
        AvacynAngelOfHope.class})
class NaturalEndTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new NaturalEnd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Destroys target artifact and gains 3 life")
    void destroysArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        prepare();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Destroys target enchantment and gains 3 life")
    void destroysEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AngelicChorus()).getId();
        prepare();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth()); // legal target so the spell is playable
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("A legal indestructible target survives but the caster gains 3 life")
    void gainsLifeWhenDestructionIsPrevented() {
        harness.addToBattlefield(player2, new AvacynAngelOfHope());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        prepare();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Natural End");
    }

    @Test
    @DisplayName("An illegal target prevents both destruction and life gain")
    void doesNotGainLifeWhenTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        prepare();
        harness.castInstant(player1, 0, targetId);

        harness.setHand(player2, List.of(new NaturalEnd()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertLife(player2, 23);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
        harness.assertInGraveyard(player1, "Natural End");
    }

    @Test
    @DisplayName("Can destroy the caster's own artifact and gain life")
    void canTargetOwnArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth()).getId();
        prepare();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }
}
