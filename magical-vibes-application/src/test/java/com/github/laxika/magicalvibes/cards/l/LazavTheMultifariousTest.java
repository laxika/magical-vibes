package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.d.DimirInformant;
import com.github.laxika.magicalvibes.cards.e.ErstwhileTrooper;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.cards.w.WallOfMist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LazavTheMultifarious.class, GrizzlyBears.class, HillGiant.class,
        Ornithopter.class, TomeScour.class, Clone.class, DimirInformant.class, WallOfMist.class,
        ErstwhileTrooper.class})
class LazavTheMultifariousTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new LazavTheMultifarious(), "{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Becomes a copy of a target creature card with mana value X, keeping its name, legendary supertype, and ability")
    void becomesCopyOfTargetCreatureCard() {
        Permanent lazav = addReadyLazav();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(lazav.getCard().getName()).isEqualTo("Lazav, the Multifarious");
        assertThat(lazav.getCard().getPower()).isEqualTo(2);
        assertThat(lazav.getCard().getToughness()).isEqualTo(2);
        assertThat(lazav.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);

        Card ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));
        harness.activateAbility(player1, 0, 0, ornithopter.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(lazav.getCard().getPower()).isZero();
        assertThat(lazav.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a noncreature or wrong-mana-value graveyard target")
    void rejectsIllegalGraveyardTarget() {
        addReadyLazav();
        Card sorcery = new TomeScour();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, sorcery.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, giant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyLazav() {
        Permanent lazav = harness.addToBattlefieldAndReturn(player1, new LazavTheMultifarious());
        lazav.setSummoningSick(false);
        return lazav;
    }

    @Test
    void surveilCanLeaveTheTopCardInTheLibrary() {
        Card topCard = new WallOfMist();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new LazavTheMultifarious(), "{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void rejectsCreatureInOpponentsGraveyard() {
        addReadyLazav();
        Card wall = new WallOfMist();
        harness.setGraveyard(player2, List.of(wall));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, wall.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetLeavingGraveyardPreventsCopying() {
        Permanent lazav = addReadyLazav();
        Card original = lazav.getCard();
        Card wall = new WallOfMist();
        harness.setGraveyard(player1, List.of(wall));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, wall.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(wall));
        harness.passBothPriorities();

        assertThat(lazav.getCard()).isSameAs(original);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyingDoesNotTriggerCopiedEnterAbilityOrChangePermanentState() {
        Permanent lazav = addReadyLazav();
        lazav.tap();
        lazav.setSummoningSick(true);
        Card informant = new DimirInformant();
        Card topCard = new WallOfMist();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(informant));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 3, informant.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(lazav.getCard().getToughness()).isEqualTo(4);
        assertThat(lazav.isTapped()).isTrue();
        assertThat(lazav.isSummoningSick()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(informant);
    }

    @Test
    void copyingGrantsAFreshOncePerTurnAbilityEachTime() {
        Permanent lazav = addReadyLazav();
        Card trooper = new ErstwhileTrooper();
        harness.setGraveyard(player1, List.of(trooper));
        harness.setHand(player1, List.of(new WallOfMist(), new WallOfMist()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 3, trooper.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, lazav)).isEqualTo(4);

        harness.activateAbility(player1, 0, 1, 3, trooper.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lazav)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cloneUsingCopiedLazavAbilityKeepsLazavName() {
        Permanent lazav = harness.addToBattlefieldAndReturn(player2, new LazavTheMultifarious());
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, lazav.getId());
        harness.passBothPriorities();
        Permanent clone = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(clone.getCard().getPower()).isEqualTo(2);
        assertThat(clone.getCard().getName()).isEqualTo("Lazav, the Multifarious");
    }

    @Test
    void cloneUsingCopiedLazavAbilityCanCopyAgain() {
        Permanent lazav = harness.addToBattlefieldAndReturn(player2, new LazavTheMultifarious());
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, lazav.getId());
        harness.passBothPriorities();
        Permanent clone = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card bears = new GrizzlyBears();
        Card wall = new WallOfMist();
        harness.setGraveyard(player1, List.of(bears, wall));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, wall.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(clone.getCard().getToughness()).isEqualTo(5);
    }
}
