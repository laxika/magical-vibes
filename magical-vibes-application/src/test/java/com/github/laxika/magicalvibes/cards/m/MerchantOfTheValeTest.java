package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Haggle;
import com.github.laxika.magicalvibes.cards.w.WeaselbackRedcap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerchantOfTheVale.class, Haggle.class, Forest.class, WeaselbackRedcap.class})
class MerchantOfTheValeTest extends BaseCardTest {

    @Test
    void haggleMayDiscardThenDrawAndExilesTheCard() {
        MerchantOfTheVale merchant = new MerchantOfTheVale();
        WeaselbackRedcap discarded = new WeaselbackRedcap();
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(merchant, discarded));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.findExiledCard(merchant.getId())).isNotNull();
    }

    @Test
    void merchantAbilityDiscardsThenDraws() {
        addCreatureReady(player1, new MerchantOfTheVale());
        WeaselbackRedcap discarded = new WeaselbackRedcap();
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void decliningHaggleDoesNotDiscardOrDrawAndStillAllowsCastingMerchant() {
        MerchantOfTheVale merchant = new MerchantOfTheVale();
        Forest kept = new Forest();
        Forest undrawn = new Forest();
        harness.setHand(player1, List.of(merchant, kept));
        harness.setLibrary(player1, List.of(undrawn));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(merchant.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, merchant.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(merchant.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(merchant.getId()));
    }

    @Test
    void haggleWithNoCardToDiscardDoesNotDraw() {
        MerchantOfTheVale merchant = new MerchantOfTheVale();
        Forest undrawn = new Forest();
        harness.setHand(player1, List.of(merchant));
        harness.setLibrary(player1, List.of(undrawn));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(merchant.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void merchantCanActivateWhileSummoningSickAndDiscardsBeforeDrawing() {
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new MerchantOfTheVale());
        merchant.setSummoningSick(true);
        Forest discarded = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(merchant.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(merchant.isTapped()).isFalse();
    }

    @Test
    void merchantCannotActivateWithoutACardToDiscard() {
        harness.addToBattlefield(player1, new MerchantOfTheVale());
        Forest undrawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(undrawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }
}
