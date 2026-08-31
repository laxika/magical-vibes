package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvatarDestiny.class, Forest.class, GrizzlyBears.class, Shock.class})
class AvatarDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature gets +1/+1 for each creature card in its controller's graveyard and becomes an Avatar")
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
        resolvePendingStackEntries();

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

    private Permanent addAttachedAura(Permanent creature) {
        Permanent aura = new Permanent(new AvatarDestiny());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void resolvePendingStackEntries() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
