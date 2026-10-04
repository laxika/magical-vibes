package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhastlyDemise.class, AvenFlock.class, DuskImp.class, Forest.class})
class GhastlyDemiseTest extends BaseCardTest {

    private void cast(List<Card> graveyard, UUID targetId) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new GhastlyDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Destroys a nonblack creature whose toughness is at most the caster's graveyard size")
    void destroysCreatureWithinGraveyardThreshold() {
        harness.addToBattlefield(player2, new AvenFlock());
        UUID targetId = harness.getPermanentId(player2, "Aven Flock");

        cast(List.of(new Forest(), new Forest(), new Forest()), targetId);

        harness.assertNotOnBattlefield(player2, "Aven Flock");
        harness.assertInGraveyard(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Can target a creature above the threshold, but does not destroy it")
    void doesNothingAboveGraveyardThreshold() {
        harness.addToBattlefield(player2, new AvenFlock());
        UUID targetId = harness.getPermanentId(player2, "Aven Flock");

        cast(List.of(new Forest(), new Forest()), targetId);

        harness.assertOnBattlefield(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Counts only the caster's graveyard")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player2, new AvenFlock());
        UUID targetId = harness.getPermanentId(player2, "Aven Flock");

        cast(List.of(), targetId);

        harness.assertOnBattlefield(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Uses the graveyard size at resolution when it increases")
    void countsCardsAddedBeforeResolution() {
        harness.addToBattlefield(player2, new AvenFlock());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GhastlyDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Aven Flock"));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aven Flock");
        harness.assertInGraveyard(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Uses the graveyard size at resolution when it decreases")
    void countsCardsRemovedBeforeResolution() {
        harness.addToBattlefield(player2, new AvenFlock());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GhastlyDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Aven Flock"));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Aven Flock");
        harness.assertInGraveyard(player1, "Ghastly Demise");
    }

    @Test
    @DisplayName("Uses increased toughness at resolution")
    void toughnessIncreaseInResponsePreventsDestruction() {
        harness.addToBattlefield(player2, new AvenFlock());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GhastlyDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Aven Flock"));
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Aven Flock");
        harness.assertInGraveyard(player1, "Ghastly Demise");
    }

    @Test
    @DisplayName("Can destroy the caster's own nonblack creature")
    void destroysOwnCreatureWithinThreshold() {
        harness.addToBattlefield(player1, new AvenFlock());

        cast(List.of(new Forest(), new Forest(), new Forest()),
                harness.getPermanentId(player1, "Aven Flock"));

        harness.assertNotOnBattlefield(player1, "Aven Flock");
        harness.assertInGraveyard(player1, "Aven Flock");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new DuskImp());
        UUID targetId = harness.getPermanentId(player2, "Dusk Imp");
        harness.setHand(player1, List.of(new GhastlyDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new GhastlyDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
