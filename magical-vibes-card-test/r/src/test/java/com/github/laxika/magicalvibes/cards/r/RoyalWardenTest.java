package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RoyalWarden.class)
class RoyalWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Royal Warden creates two tapped Necron Warrior tokens when it enters")
    void entersCreatesTwoTappedTokens() {
        harness.setHand(player1, List.of(new RoyalWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(Permanent::isTapped);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.NECRON, CardSubtype.WARRIOR);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unearth returns Royal Warden with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new RoyalWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent warden = findPermanent(player1, "Royal Warden");
        assertThat(warden.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(warden.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Royal Warden");
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2).allMatch(Permanent::isTapped);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Royal Warden");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Royal Warden"));
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrderElementsOf(tokens);
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthCannotBeActivatedOutsideMainPhase() {
        harness.setGraveyard(player1, List.of(new RoyalWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Royal Warden");
        harness.assertNotOnBattlefield(player1, "Royal Warden");
    }

    @Test
    @DisplayName("Unearth cannot be activated while a spell is on the stack")
    void unearthCannotBeActivatedWithNonemptyStack() {
        harness.setGraveyard(player1, List.of(new RoyalWarden()));
        harness.setHand(player1, List.of(new RoyalWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Royal Warden");
    }

    @Test
    @DisplayName("An unearthed Royal Warden is exiled instead of dying")
    void unearthedWardenIsExiledInsteadOfDying() {
        harness.setGraveyard(player1, List.of(new RoyalWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent warden = findPermanent(player1, "Royal Warden");
        warden.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Royal Warden");
        harness.assertNotInGraveyard(player1, "Royal Warden");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(warden.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> permanent.getCard().isToken());
    }
}
