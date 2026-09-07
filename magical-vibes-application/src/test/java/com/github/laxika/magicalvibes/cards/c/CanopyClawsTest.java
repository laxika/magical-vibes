package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

@CardUsed({CanopyClaws.class, SuntailHawk.class, FountainOfYouth.class})
class CanopyClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature loses flying until end of turn")
    void removesFlyingUntilEndOfTurn() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new CanopyClaws()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent hawk = findPermanent(player2, "Suntail Hawk");
        assertThat(hawk.hasKeyword(Keyword.FLYING)).isFalse();

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
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Suntail Hawk").hasKeyword(Keyword.FLYING)).isFalse();
        harness.assertNotInGraveyard(player1, "Canopy Claws");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Canopy Claws"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CanopyClaws()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
