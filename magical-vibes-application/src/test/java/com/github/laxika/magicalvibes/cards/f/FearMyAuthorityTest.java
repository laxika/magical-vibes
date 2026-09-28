package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearMyAuthority.class, GrizzlyBears.class, Shock.class})
class FearMyAuthorityTest extends BaseCardTest {

    @Test
    void buffsOwnCreaturesAndGivesThemFear() {
        harness.addToBattlefield(player1, new FearMyAuthority());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FEAR)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FEAR)).isFalse();
    }

    @Test
    void controllerMayDiscardToKeepScheme() {
        Permanent scheme = addScheme();
        Card discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLife(player1, 20);

        resolveUpkeep(scheme);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void controllerMayPayLifeToKeepScheme() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of(new Shock()));
        harness.setLife(player1, 20);

        resolveUpkeep(scheme);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void schemeIsAbandonedWhenControllerCanDoNeither() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of());
        harness.setLife(player1, 2);

        resolveUpkeep(scheme);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scheme.getCard());
    }

    private Permanent addScheme() {
        return harness.addToBattlefieldAndReturn(player1, new FearMyAuthority());
    }

    private void resolveUpkeep(Permanent scheme) {
        Card sourceCard = scheme.getCard();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                player1.getId(),
                sourceCard.getName() + "'s upkeep ability",
                sourceCard.getEffects(EffectSlot.UPKEEP_TRIGGERED),
                (UUID) null,
                scheme.getId()));
        harness.passBothPriorities();
    }
}
