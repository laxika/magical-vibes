package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KitsaOtterballElite;
import com.github.laxika.magicalvibes.cards.j.JunkbladeBruiser;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.p.PestilenceRats;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplashPortal.class, ZephyrFalcon.class, Frogmite.class, KitsaOtterballElite.class,
        PestilenceRats.class, GrizzlyBears.class, JunkbladeBruiser.class, MaskwoodNexus.class})
class SplashPortalTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers each listed creature subtype and draws a card")
    void flickersListedCreatureSubtypesAndDraws() {
        assertFlickersAndDraws(new ZephyrFalcon());
        assertFlickersAndDraws(new Frogmite());
        assertFlickersAndDraws(new KitsaOtterballElite());
        assertFlickersAndDraws(new PestilenceRats());
    }

    @Test
    @DisplayName("Flickers an unlisted creature without drawing a card")
    void flickersUnlistedCreatureWithoutDrawing() {
        assertFlickersWithoutDrawing(new GrizzlyBears());
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsForCreatureGivenAllTypesByContinuousEffect() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        assertFlickersAndDraws(new JunkbladeBruiser());
    }

    @Test
    void exiledChangelingTokenDoesNotReturnOrDraw() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Shapeshifter");
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, token.getId());

        assertThat(countPermanents(player1, "Shapeshifter")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsAsNewUntappedPermanentWithoutCountersOrDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JunkbladeBruiser());
        target.tap();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        Permanent returned = findPermanent(player1, "Junkblade Bruiser");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getMarkedDamage()).isZero();
    }

    @Test
    void stolenCreatureReturnsToOwnerAndCasterDraws() {
        Card creature = new ZephyrFalcon();
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZephyrFalcon());
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getOriginalCard()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getOriginalCard());
    }

    private void assertFlickersAndDraws(Card creature) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private void assertFlickersWithoutDrawing(Card creature) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }
}
