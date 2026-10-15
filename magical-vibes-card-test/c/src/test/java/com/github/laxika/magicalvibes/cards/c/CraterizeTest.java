package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Craterize.class, Mountain.class, WalkingCorpse.class})
class CraterizeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvingDestroysTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new Craterize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Can destroy own land")
    void canDestroyOwnLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new Craterize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Craterize()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID creatureId = harness.getPermanentId(player2, "Walking Corpse");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not destroy another land when the target leaves before resolution")
    void targetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        UUID otherLandId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new Craterize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(targetId));
        harness.setGraveyard(player2, List.of(new Mountain()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(otherLandId);
        harness.assertInGraveyard(player1, "Craterize");
    }
}
