package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrilliantHalo.class, ArgothianSwine.class, Plains.class, Duress.class})
class BrilliantHaloTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Brilliant Halo attaches it and boosts the enchanted creature")
    void resolvingAttachesAndBoostsCreature() {
        Permanent swine = new Permanent(new ArgothianSwine());
        gd.playerBattlefields.get(player1.getId()).add(swine);
        harness.setHand(player1, List.of(new BrilliantHalo()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, swine.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Brilliant Halo");
        assertThat(aura.getAttachedTo()).isEqualTo(swine.getId());
        assertThat(gqs.getEffectivePower(gd, swine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, swine)).isEqualTo(5);
    }

    @Test
    @DisplayName("When Brilliant Halo is put into a graveyard from the battlefield, it returns to its owner's hand")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent swine = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());
        BrilliantHalo haloCard = new BrilliantHalo();
        haloCard.setOwnerId(player1.getId());
        Permanent aura = new Permanent(haloCard);
        aura.setAttachedTo(swine.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Brilliant Halo");
        harness.assertNotInGraveyard(player1, "Brilliant Halo");
        harness.assertNotInHand(player2, "Brilliant Halo");
        harness.assertNotOnBattlefield(player1, "Brilliant Halo");
    }

    @Test
    @DisplayName("Brilliant Halo cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new BrilliantHalo()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Putting Brilliant Halo into a graveyard from hand does not return it")
    void doesNotReturnWhenPutIntoGraveyardFromHand() {
        harness.setHand(player1, List.of(new BrilliantHalo()));
        harness.setHand(player2, List.of(new Duress()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Brilliant Halo");
        harness.assertNotInHand(player1, "Brilliant Halo");
        assertThat(gd.stack).isEmpty();
    }
}
