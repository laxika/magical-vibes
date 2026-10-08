package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LoyalFireSage;
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

@CardUsed({WaterbendersRestoration.class, LoyalFireSage.class})
class WaterbendersRestorationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles exactly X controlled creatures and returns them at the next end step")
    void exilesExactlyXCreaturesAndReturnsThemAtNextEndStep() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        Permanent waterbendSourceOne = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        Permanent waterbendSourceTwo = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 2, null, null,
                List.of(firstTarget.getId(), secondTarget.getId()), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(waterbendSourceOne.getId(), waterbendSourceTwo.getId()));
        assertThat(waterbendSourceOne.isTapped()).isTrue();
        assertThat(waterbendSourceTwo.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(untargeted)
                .doesNotContain(firstTarget, secondTarget);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Loyal Fire Sage", "Loyal Fire Sage");

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(untargeted)
                .hasSize(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Allows X to be zero")
    void allowsXToBeZero() {
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LoyalFireSage());
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Waterbend can be paid entirely with mana")
    void paysWaterbendWithMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        advanceToEndStep();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Loyal Fire Sage");
    }

    @Test
    @DisplayName("All exiled cards share one return trigger even when their owners differ")
    void returnsDifferentOwnersCardsWithOneTrigger() {
        Permanent owned = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new LoyalFireSage());
        gd.playerBattlefields.get(player2.getId()).remove(stolen);
        gd.playerBattlefields.get(player1.getId()).add(stolen);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstantForX(player1, 0, 2, List.of(owned.getId(), stolen.getId()));
        harness.passBothPriorities();
        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Loyal Fire Sage");
        harness.assertOnBattlefield(player2, "Loyal Fire Sage");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Requires exactly X distinct targets")
    void rejectsTooFewTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A targeted summoning-sick creature can pay its own waterbend cost")
    void targetCanPayWaterbend() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        target.setSummoningSick(true);
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 1, null, null,
                List.of(target.getId()), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(target.getId()));
        assertThat(target.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        advanceToEndStep();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(returned -> assertThat(returned.isTapped()).isFalse());
    }

    @Test
    @DisplayName("Other exiled cards return if the first card leaves and reenters exile in response to the trigger")
    void returnsRemainingCardsWhenFirstReentersExile() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.removeFromExile(first.getCard().getId())).isTrue();
        gd.addToExile(player1.getId(), first.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(second.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first.getCard());
    }

    private void advanceToEndStep() {
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
    }
}
