package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceTransformedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoyalCatharUnhallowedCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Has death trigger and back face cannot block")
    void hasConfiguredEffects() {
        LoyalCatharUnhallowedCathar card = new LoyalCatharUnhallowedCathar();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).singleElement()
                .isInstanceOf(RegisterDelayedReturnSourceTransformedEffect.class);
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("UnhallowedCathar");
        assertThat(card.getBackFaceCard().getEffects(EffectSlot.STATIC)).singleElement()
                .isInstanceOf(CantBlockEffect.class);
    }

    @Test
    @DisplayName("Returns transformed at the beginning of the next end step")
    void returnsTransformedAtNextEndStep() {
        harness.addToBattlefield(player1, new LoyalCatharUnhallowedCathar());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Loyal Cathar"));
        harness.passBothPriorities(); // resolve Lightning Bolt
        harness.passBothPriorities(); // resolve death trigger and register delayed return

        assertThat(findPermanentOrNull(player1, "Loyal Cathar")).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Loyal Cathar"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities(); // advance to end step and process delayed return

        Permanent returned = findPermanent(player1, "Unhallowed Cathar");
        assertThat(returned).isNotNull();
        assertThat(returned.isTransformed()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Loyal Cathar"));
    }

    @Test
    @DisplayName("Does not return if the card leaves the graveyard before the end step")
    void doesNotReturnIfNoLongerInGraveyard() {
        harness.addToBattlefield(player1, new LoyalCatharUnhallowedCathar());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Loyal Cathar"));
        harness.passBothPriorities(); // resolve Lightning Bolt
        harness.passBothPriorities(); // resolve death trigger and register delayed return

        gd.playerGraveyards.get(player1.getId()).clear();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities(); // advance to end step

        assertThat(findPermanentOrNull(player1, "Unhallowed Cathar")).isNull();
    }

    @Test
    @DisplayName("Unhallowed Cathar cannot block")
    void unhallowedCatharCannotBlock() {
        Permanent cathar = harness.addToBattlefieldAndReturn(player1, new LoyalCatharUnhallowedCathar());
        cathar.setCard(cathar.getOriginalCard().getBackFaceCard());
        cathar.setTransformed(true);
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.canBlockAttacker(gd, cathar, findPermanent(player2, "Grizzly Bears"),
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    private Permanent findPermanentOrNull(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
