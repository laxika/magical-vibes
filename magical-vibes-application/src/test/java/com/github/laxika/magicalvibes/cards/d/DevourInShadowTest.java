package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevourInShadow.class, DrossCrocodile.class, ConjurersBauble.class})
class DevourInShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and the spell's controller loses life equal to its toughness")
    void destroysCreatureAndLosesLifeEqualToToughness() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        target.setToughnessModifier(2);
        prepareDevourInShadow();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Dross Crocodile");
        harness.assertInGraveyard(player2, "Dross Crocodile");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroys a creature with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        target.setRegenerationShield(1);
        prepareDevourInShadow();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Dross Crocodile");
        harness.assertInGraveyard(player2, "Dross Crocodile");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Still makes the spell's controller lose life when the target is indestructible")
    void losesLifeWhenTargetIsIndestructible() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        prepareDevourInShadow();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Dross Crocodile");
        harness.assertNotInGraveyard(player2, "Dross Crocodile");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ConjurersBauble());
        prepareDevourInShadow();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        prepareDevourInShadow();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Devour in Shadow");
    }

    private void prepareDevourInShadow() {
        harness.setHand(player1, List.of(new DevourInShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
