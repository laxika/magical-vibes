package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnuridBrushhopper;
import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CanopyClaws.class, SuntailHawk.class, AnuridBrushhopper.class, RiftstonePortal.class})
class CanopyClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature loses flying until end of turn")
    void removesFlyingUntilEndOfTurn() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new CanopyClaws()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent hawk = findPermanent(player2, "Suntail Hawk");
        assertThat(hawk.hasKeyword(Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player1, "Canopy Claws");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hawk.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flashback removes flying and exiles Canopy Claws")
    void flashbackRemovesFlyingAndExilesSpell() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setGraveyard(player1, List.of(new CanopyClaws()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.castAndResolveFlashback(player1, 0, targetId);

        assertThat(findPermanent(player2, "Suntail Hawk").hasKeyword(Keyword.FLYING)).isFalse();
        harness.assertNotInGraveyard(player1, "Canopy Claws");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Canopy Claws"));
    }

    @Test
    @DisplayName("Only the targeted creature loses flying")
    void onlyTargetedCreatureLosesFlying() {
        Permanent targetedHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent otherHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new CanopyClaws()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetedHawk.getId());

        assertThat(targetedHawk.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(otherHawk.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can target a creature that does not have flying")
    void canTargetCreatureWithoutFlying() {
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new AnuridBrushhopper());
        harness.setHand(player1, List.of(new CanopyClaws()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, groundCreature.getId());

        assertThat(groundCreature.hasKeyword(Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player1, "Canopy Claws");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RiftstonePortal());
        harness.setHand(player1, List.of(new CanopyClaws()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
