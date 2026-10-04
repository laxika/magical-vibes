package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        resolveUpkeep();

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

        resolveUpkeep();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void schemeIsAbandonedWhenControllerCanDoNeither() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of());
        harness.setLife(player1, 2);

        resolveUpkeep();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme.getCard());
    }

    @Test
    void mayDeclineLifePaymentWhenHandIsEmpty() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        resolveUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme.getCard());
        harness.assertLife(player1, 20);
    }

    @Test
    void mayDeclineDiscardWhenLifePaymentIsImpossible() {
        Permanent scheme = addScheme();
        Card card = new Shock();
        harness.setHand(player1, List.of(card));
        harness.setLife(player1, 2);

        resolveUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertLife(player1, 2);
    }

    @Test
    void faceUpSchemeInCommandZoneBuffsCreatures() {
        gd.playerCommandZones.get(player1.getId()).add(new FearMyAuthority());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();
    }

    @Test
    void faceUpSchemeInCommandZoneTriggersAtControllersUpkeep() {
        gd.playerCommandZones.get(player1.getId()).add(new FearMyAuthority());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLife(player1, 20);

        resolveUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void fearPreventsBlockingByNonblackNonartifactCreature() {
        harness.addToBattlefield(player1, new FearMyAuthority());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    private Permanent addScheme() {
        return harness.addToBattlefieldAndReturn(player1, new FearMyAuthority());
    }

    private void resolveUpkeep() {
        advanceToUpkeep(player1);
        resolveAllTriggers();
    }
}
