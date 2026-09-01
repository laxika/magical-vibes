package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OgreMenial;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SchemingFence.class, OgreMenial.class})
class SchemingFenceTest extends BaseCardTest {

    @Test
    void choosesAnExistingNonlandPermanentAndGainsItsNonLoyaltyActivatedAbilities() {
        Permanent ogre = addReady(new OgreMenial());
        Permanent fence = castFence();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ogre.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());

        harness.handlePermanentChosen(player1, ogre.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fence)).isEqualTo(3);
    }

    @Test
    void preventsTheChosenPermanentFromActivatingItsAbilities() {
        Permanent ogre = addReady(new OgreMenial());
        castFence();
        harness.handlePermanentChosen(player1, ogre.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                   .isInstanceOf(IllegalStateException.class)
                   .hasMessageContaining("can't be activated");
    }

    @Test
    void mayDeclineToChooseAPermanent() {
        addReady(new OgreMenial());
        Permanent fence = castFence();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(fence.getChosenPermanentId()).isNull();
    }

    private Permanent addReady(Card card) {
        return addCreatureReady(player1, card);
    }

    private Permanent castFence() {
        harness.setHand(player1, List.of(new SchemingFence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Scheming Fence");
    }
}
