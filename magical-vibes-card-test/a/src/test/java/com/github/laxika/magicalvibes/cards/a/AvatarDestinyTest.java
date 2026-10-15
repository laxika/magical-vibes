package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TormodTheDesecrator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvatarDestiny.class, Forest.class, GrizzlyBears.class, Shock.class,
        Ornithopter.class, TormodTheDesecrator.class})
class AvatarDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature gets +1/+1 for each creature card in the Aura controller's graveyard and becomes an Avatar")
    void grantsGraveyardBoostAndAvatarSubtype() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        Permanent aura = addAttachedAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes())
                .contains(CardSubtype.AVATAR);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("When the enchanted creature dies, it mills its power, returns one milled creature, and returns the Aura to hand")
    void millsByLastKnownPowerReturnsOneCreatureAndAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent aura = addAttachedAura(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        Card firstMilledCreature = new GrizzlyBears();
        Card nonCreature = new Shock();
        Card secondMilledCreature = new GrizzlyBears();
        Card fourthMilledCard = new Forest();
        harness.setLibrary(player1, List.of(
                firstMilledCreature, nonCreature, secondMilledCreature, fourthMilledCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                firstMilledCreature.getId(), secondMilledCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstMilledCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(firstMilledCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(secondMilledCreature.getId(), nonCreature.getId(), fourthMilledCard.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(aura.getCard().getId());
    }

    @Test
    void canEnchantYourCreatureThroughSpellResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AvatarDestiny()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Avatar Destiny").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes()).contains(CardSubtype.AVATAR);
    }

    @Test
    void cannotEnchantAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AvatarDestiny()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Avatar Destiny");
    }

    @Test
    void boostUpdatesWithYourGraveyardAndIgnoresOpponentsGraveyard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAttachedAura(creature);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void mayDeclineCreatureReturnWithoutLosingAuraReturn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAttachedAura(creature);
        Card milledCreature = new GrizzlyBears();
        Card milledLand = new Forest();
        Card unMilledCard = new Shock();
        harness.setLibrary(player1, List.of(milledCreature, milledLand, unMilledCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(milledCreature.getId(), milledLand.getId(), creature.getCard().getId())
                .doesNotContain(aura.getCard().getId(), unMilledCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(unMilledCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(aura.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsAuraEvenWhenNoCreatureWasMilled() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAttachedAura(creature);
        Card land = new Forest();
        Card spell = new Shock();
        harness.setLibrary(player1, List.of(land, spell));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(land.getId(), spell.getId()).doesNotContain(aura.getCard().getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(aura.getCard().getId());
    }

    @Test
    @CardUsed({Ornithopter.class})
    void zeroPowerDeathReturnsAuraWithoutMilling() {
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        Permanent aura = addAttachedAura(creature);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(topCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(aura.getCard().getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({TormodTheDesecrator.class})
    void auraAndMilledCreatureLeaveGraveyardSimultaneously() {
        addCreatureReady(player1, new TormodTheDesecrator());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAttachedAura(creature);
        Card milledCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milledCreature, new Forest()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(milledCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(aura.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(milledCreature.getId()));
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanent(player1, "Zombie").isTapped()).isTrue();
    }

    private Permanent addAttachedAura(Permanent creature) {
        Permanent aura = new Permanent(new AvatarDestiny());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
