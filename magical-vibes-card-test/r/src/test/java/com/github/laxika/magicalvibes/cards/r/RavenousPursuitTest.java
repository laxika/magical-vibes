package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Malignus;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousPursuit.class, ColossalDreadmaw.class, GrizzlyBears.class, HillGiant.class,
        Malignus.class, PaladinEnVec.class})
class RavenousPursuitTest extends BaseCardTest {

    @Test
    void dealsExcessDamageAndPerpetuallyBoostsTheChosenCreatureCard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears inHand = new GrizzlyBears();
        HillGiant other = new HillGiant();
        harness.setHand(player1, List.of(new RavenousPursuit(), inHand, other));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).cards())
                .extracting(Card::getId).containsExactly(inHand.getId(), other.getId());
        harness.handleMultipleCardsChosen(player1, List.of(inHand.getId()));
        assertThat(boostOf(inHand)).isEqualTo(new CardPowerToughnessModifier(4, 4));
        assertThat(boostOf(other)).isEqualTo(new CardPowerToughnessModifier(0, 0));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent boosted = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(6);
    }

    @Test
    void choosesCreatureEvenWhenNoExcessDamageWasDealt() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears inHand = new GrizzlyBears();
        harness.setHand(player1, List.of(new RavenousPursuit(), inHand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(boostOf(inHand)).isEqualTo(new CardPowerToughnessModifier(0, 0));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void countsDamageAlreadyMarkedWhenDeterminingExcess() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setMarkedDamage(2);
        GrizzlyBears inHand = new GrizzlyBears();
        harness.setHand(player1, List.of(new RavenousPursuit(), inHand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(boostOf(inHand)).isEqualTo(new CardPowerToughnessModifier(5, 5));
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void stillChoosesACreatureWithZeroBonusWhenTheDamageSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears inHand = new GrizzlyBears();
        harness.setHand(player1, List.of(new RavenousPursuit(), inHand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(boostOf(inHand)).isEqualTo(new CardPowerToughnessModifier(0, 0));
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void dealsDamageWithoutACreatureCardInHand() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RavenousPursuit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), target.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ravenous Pursuit");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    void unpreventableCreatureDamageIgnoresProtectionFromTheCreatureColor() {
        harness.setLife(player2, 20);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Malignus());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        GrizzlyBears inHand = new GrizzlyBears();
        harness.setHand(player1, List.of(new RavenousPursuit(), inHand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(boostOf(inHand)).isEqualTo(new CardPowerToughnessModifier(8, 8));
        harness.assertInGraveyard(player2, "Paladin en-Vec");
    }

    @Test
    void requiresAControlledCreatureAsTheFirstTarget() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears inHand = new GrizzlyBears();
        harness.setHand(player1, List.of(new RavenousPursuit(), inHand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(opponentCreature.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private CardPowerToughnessModifier boostOf(Card card) {
        return gd.perpetualCardPowerToughnessModifiers.getOrDefault(card.getId(), new CardPowerToughnessModifier(0, 0));
    }
}
