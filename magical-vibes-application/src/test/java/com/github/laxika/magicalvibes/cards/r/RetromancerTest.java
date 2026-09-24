package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishHerder;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishHerder.class, Humble.class, Retromancer.class, RuneOfProtectionRed.class})
class RetromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the controller of a spell that targets it")
    void damagesSpellController() {
        Permanent retromancer = harness.addToBattlefieldAndReturn(player1, new Retromancer());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, retromancer.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals 3 damage to the controller of an ability that targets it")
    void damagesAbilityController() {
        Permanent retromancer = harness.addToBattlefieldAndReturn(player1, new Retromancer());
        Permanent herder = harness.addToBattlefieldAndReturn(player2, new ElvishHerder());
        herder.setSummoningSick(false);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, null, retromancer.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Its damage can be prevented")
    void damageCanBePrevented() {
        Permanent retromancer = harness.addToBattlefieldAndReturn(player1, new Retromancer());
        Permanent rune = harness.addToBattlefieldAndReturn(player2, new RuneOfProtectionRed());
        rune.setSummoningSick(false);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player2, retromancer.getId());

        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, retromancer.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }
}
