package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhantasmalImageTest extends BaseCardTest {

    private Permanent copyGrizzlyBears() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhantasmalImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phantasmal Image"))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Phantasmal Image enters as a copy of a creature and is an Illusion in addition to its other types")
    void copiesCreatureAndIsIllusion() {
        Permanent image = copyGrizzlyBears();

        assertThat(image).isNotNull();
        assertThat(image.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(image.getCard().getPower()).isEqualTo(2);
        assertThat(image.getCard().getToughness()).isEqualTo(2);
        assertThat(image.getCard().getSubtypes())
                .contains(CardSubtype.BEAR, CardSubtype.ILLUSION);
    }

    @Test
    @DisplayName("The copy is sacrificed when it becomes the target of a spell")
    void sacrificedWhenTargetedBySpell() {
        Permanent image = copyGrizzlyBears();
        assertThat(image).isNotNull();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, image.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Phantasmal Image"));
        harness.assertInGraveyard(player1, "Phantasmal Image");
        // The copied original is untouched
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Phantasmal Image enters as a 0/0 and dies when the controller declines to copy")
    void diesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhantasmalImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Phantasmal Image"));
        harness.assertInGraveyard(player1, "Phantasmal Image");
    }
}
