package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ShatteredWings.class, FountainOfYouth.class, AngelicChorus.class, AirElemental.class,
        GrizzlyBears.class})
class ShatteredWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        castAt(harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()));

        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Destroys a target enchantment")
    void destroysEnchantment() {
        castAt(harness.addToBattlefieldAndReturn(player2, new AngelicChorus()));

        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Destroys a target creature with flying")
    void destroysFlyingCreature() {
        castAt(harness.addToBattlefieldAndReturn(player2, new AirElemental()));

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShatteredWings()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or creature with flying");
    }

    @Test
    @DisplayName("Surveils one card after destroying the target")
    void surveilsAfterDestroyingTarget() {
        Card topCard = new GrizzlyBears();
        Card keptCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard, keptCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castAt(target);
        harness.assertInGraveyard(player2, "Fountain of Youth");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new ShatteredWings()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
