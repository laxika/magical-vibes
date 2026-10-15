package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({GoldmeadowLookout.class, GroveOfTheBurnwillows.class})
class GoldmeadowLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Activating creates a Goldmeadow Harrier token after discarding a card")
    void activatingCreatesGoldmeadowHarrierToken() {
        Permanent lookout = addCreatureReady(player1, new GoldmeadowLookout());
        GoldmeadowLookout discardedCard = new GoldmeadowLookout();
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(lookout.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);

        Permanent token = findPermanent(player1, "Goldmeadow Harrier");
        assertThat(token.getCard().getName()).isEqualTo("Goldmeadow Harrier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KITHKIN, CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Goldmeadow Harrier token can tap a creature")
    void tokenAbilityTapsTargetCreature() {
        addCreatureReady(player1, new GoldmeadowLookout());
        harness.setHand(player1, List.of(new GoldmeadowLookout()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Goldmeadow Harrier");
        token.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldmeadowLookout());
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, tokenIndex, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(token.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Goldmeadow Harrier token cannot target a noncreature permanent")
    void tokenAbilityCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new GoldmeadowLookout());
        harness.setHand(player1, List.of(new GoldmeadowLookout()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Goldmeadow Harrier");
        token.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GroveOfTheBurnwillows());
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, tokenIndex, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new GoldmeadowLookout());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land can pay the discard cost and the token is created on resolution")
    void canDiscardLandToCreateToken() {
        Permanent lookout = addCreatureReady(player1, new GoldmeadowLookout());
        GroveOfTheBurnwillows discardedCard = new GroveOfTheBurnwillows();
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(lookout.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(countPermanents(player1, "Goldmeadow Harrier")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Goldmeadow Harrier")).isEqualTo(1);
        assertThat(findPermanent(player1, "Goldmeadow Harrier").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Newly created Harrier cannot activate its tap ability immediately")
    void newlyCreatedTokenCannotActivate() {
        addCreatureReady(player1, new GoldmeadowLookout());
        harness.setHand(player1, List.of(new GoldmeadowLookout()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Goldmeadow Harrier");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldmeadowLookout());
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, tokenIndex, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(token.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Harrier can tap a creature controlled by its controller")
    void tokenCanTapOwnCreature() {
        Permanent lookout = addCreatureReady(player1, new GoldmeadowLookout());
        harness.setHand(player1, List.of(new GoldmeadowLookout()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Goldmeadow Harrier");
        token.setSummoningSick(false);
        lookout.untap();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, tokenIndex, 0, null, lookout.getId());
        harness.passBothPriorities();

        assertThat(token.isTapped()).isTrue();
        assertThat(lookout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lookout cannot activate while summoning sick")
    void summoningSickLookoutCannotActivate() {
        harness.addToBattlefield(player1, new GoldmeadowLookout());
        harness.setHand(player1, List.of(new GoldmeadowLookout()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Goldmeadow Harrier")).isZero();
    }
}
