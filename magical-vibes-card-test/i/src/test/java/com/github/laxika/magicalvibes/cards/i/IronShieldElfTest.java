package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.f.FeedTheFlames;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronShieldElf.class, GrizzlyBears.class, Ornithopter.class,
        FeedTheFlames.class, NamelessInversion.class})
class IronShieldElfTest extends BaseCardTest {

    @Test
    void activationRequiresDiscardingACard() {
        addElfReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 1);
    }

    @Test
    void resolvingAbilityDiscardsCardTapsElfAndGrantsIndestructible() {
        Permanent elf = addElfReady(player1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(elf.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void indestructibleResetsAtEndOfTurn() {
        Permanent elf = addElfReady(player1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        addElfReady(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    void discardIsPaidBeforeResolutionButTappingAndIndestructibleAreNot() {
        Permanent elf = addElfReady(player1);
        harness.setHand(player1, List.of(new IronShieldElf()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Iron-Shield Elf");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(elf.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(elf.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new IronShieldElf());
        elf.setSummoningSick(true);
        elf.tap();
        harness.setHand(player1, List.of(new IronShieldElf()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(elf.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Iron-Shield Elf");
    }

    @Test
    void resolvedAbilityProtectsAgainstLethalDamage() {
        Permanent elf = addElfReady(player1);
        harness.setHand(player1, List.of(new IronShieldElf()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new FeedTheFlames()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstant(player2, 0, elf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elf);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void lethalDamageInResponseKillsElfBeforeItGainsIndestructible() {
        Permanent elf = addElfReady(player1);
        harness.setHand(player1, List.of(new IronShieldElf()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, List.of(new FeedTheFlames()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstant(player2, 0, elf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elf);
        assertThat(gd.stack).hasSize(1);

        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new IronShieldElf());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(replacement.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void indestructibleDoesNotPreventDyingFromZeroOrLessToughness() {
        Permanent elf = addElfReady(player1);
        harness.setHand(player1, List.of(new IronShieldElf()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, elf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elf);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elf.getCard());
    }

    private Permanent addElfReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new IronShieldElf());
        perm.setSummoningSick(false);
        return perm;
    }
}
