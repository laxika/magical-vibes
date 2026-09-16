package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FountainOfIchor.class)
class FountainOfIchorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Fountain of Ichor adds one mana of the chosen color")
    void tapsForAnyColor() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfIchor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(fountain.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying three mana makes Fountain of Ichor a 3/3 Dinosaur artifact creature")
    void animatesIntoDinosaur() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfIchor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fountain)).isTrue();
        assertThat(gqs.isArtifact(fountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fountain)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, fountain, CardSubtype.DINOSAUR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fountain)).isFalse();
        assertThat(gqs.isArtifact(fountain)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, fountain, CardSubtype.DINOSAUR)).isFalse();
    }
}
