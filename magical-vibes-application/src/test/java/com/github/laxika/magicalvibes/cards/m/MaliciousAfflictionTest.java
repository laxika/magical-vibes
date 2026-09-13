package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaliciousAffliction.class, GrizzlyBears.class, WalkingCorpse.class})
class MaliciousAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target nonblack creature without morbid")
    void destroysTargetWithoutMorbid() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Morbid offers a copy that can be retargeted")
    void morbidCopiesAndRetargetsSpell() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, firstTarget.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondTarget.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
