package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ColossusHammer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KembasOutfitter.class, ColossusHammer.class, GrizzlyBears.class})
class KembasOutfitterTest extends BaseCardTest {

    @Test
    void firstModePerpetuallyGrantsEquipToEquipmentCardInHand() {
        ColossusHammer hammer = new ColossusHammer();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KembasOutfitter(), hammer));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualActivatedAbilityCardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent hammerPermanent = findPermanent(player1, "Colossus Hammer");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, hammerPermanent), 1,
                null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammerPermanent.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void secondModePerpetuallyGrantsEquipToTargetEquipment() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new ColossusHammer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KembasOutfitter()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, 1, hammer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void secondModeCannotTargetOpponentEquipment() {
        Permanent opponentHammer = harness.addToBattlefieldAndReturn(player2, new ColossusHammer());
        harness.setHand(player1, List.of(new KembasOutfitter()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 1, opponentHammer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
