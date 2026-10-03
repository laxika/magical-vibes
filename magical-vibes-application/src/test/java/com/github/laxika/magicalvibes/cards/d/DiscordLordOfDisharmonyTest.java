package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscordLordOfDisharmony.class, GrizzlyBears.class, ActOfTreason.class, TurnToFrog.class})
class DiscordLordOfDisharmonyTest extends BaseCardTest {

    @Test
    @DisplayName("creates a random nonland copy with temporary any-mana exile permission")
    void createsRandomNonlandCopy() {
        Permanent discord = harness.addToBattlefieldAndReturn(player1, new DiscordLordOfDisharmony());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        Card copy = gd.getPlayerExiledCards(player1.getId()).stream().findFirst().orElseThrow();
        assertThat(copy.hasType(CardType.LAND)).isFalse();
        assertThat(gd.exilePlayPermissions).containsEntry(copy.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaType).contains(copy.getId());
        assertThat(gd.discordCopySourcePermanents).containsEntry(copy.getId(), discord.getId());
    }

    @Test
    @DisplayName("casting its tracked copy queues another ability while Discord remains")
    void castingTrackedCopyQueuesAnotherAbility() {
        Permanent discord = harness.addToBattlefieldAndReturn(player1, new DiscordLordOfDisharmony());
        Card copy = new GrizzlyBears();
        gd.addToExile(player1.getId(), copy);
        gd.exilePlayPermissions.put(copy.getId(), player1.getId());
        gd.exilePlayPermissionsExpireAtTurnEnd.put(copy.getId(), gd.turnNumber + 1);
        gd.exilePlayAnyManaType.add(copy.getId());
        gd.discordCopySourcePermanents.put(copy.getId(), discord.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, copy.getId());

        assertThat(gd.discordCopySourcePermanents).doesNotContainKey(copy.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getSourcePermanentId() != null
                && entry.getSourcePermanentId().equals(discord.getId())
                && entry.getCard().getName().equals("Discord, Lord of Disharmony"));
        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
    }

    @Test
    void permissionEndsWhenNextEndStepBegins() {
        harness.addToBattlefield(player1, new DiscordLordOfDisharmony());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        Card copy = gd.getPlayerExiledCards(player1.getId()).getFirst();

        gd.turnNumber += 2;
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(copy.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(copy.getId());
    }

    @Test
    void castingCopyStillQueuesAbilityAfterDiscordLosesAbilities() {
        Permanent discord = harness.addToBattlefieldAndReturn(player1, new DiscordLordOfDisharmony());
        Card copy = prepareTrackedCopy(discord);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, discord.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromExile(player1, copy.getId());

        assertThat(gd.stack).anyMatch(entry -> discord.getId().equals(entry.getSourcePermanentId())
                && player1.getId().equals(entry.getControllerId()));
    }

    @Test
    void castingCopyStillQueuesAbilityAfterDiscordChangesController() {
        Permanent discord = harness.addToBattlefieldAndReturn(player1, new DiscordLordOfDisharmony());
        Card copy = prepareTrackedCopy(discord);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player2, 0, discord.getId());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromExile(player1, copy.getId());

        assertThat(gd.stack).anyMatch(entry -> discord.getId().equals(entry.getSourcePermanentId())
                && player1.getId().equals(entry.getControllerId()));
    }

    @Test
    void castCreatureCopyResolvesAsTokenThatWasCast() {
        Permanent discord = harness.addToBattlefieldAndReturn(player1, new DiscordLordOfDisharmony());
        Card copy = prepareTrackedCopy(discord);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromExile(player1, copy.getId());
        resolveAllTriggers();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getCard().isToken()).isTrue();
        assertThat(bears.isCast()).isTrue();
    }

    @Test
    void castingCopyWithoutDiscordDoesNotQueueAbility() {
        Permanent discord = harness.addToBattlefieldAndReturn(player1, new DiscordLordOfDisharmony());
        Card copy = prepareTrackedCopy(discord);
        gd.playerBattlefields.get(player1.getId()).remove(discord);
        gd.playerGraveyards.get(player1.getId()).add(discord.getCard());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromExile(player1, copy.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void castingCopyStillRequiresPayingItsManaCost() {
        Permanent discord = harness.addToBattlefieldAndReturn(player1, new DiscordLordOfDisharmony());
        Card copy = prepareTrackedCopy(discord);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copy.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(copy);
        assertThat(gd.exilePlayPermissions).containsEntry(copy.getId(), player1.getId());
    }

    @Test
    void castingOrdinarySpellDoesNotQueueAnotherRandomChoice() {
        harness.addToBattlefield(player1, new DiscordLordOfDisharmony());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private Card prepareTrackedCopy(Permanent discord) {
        Card copy = new GrizzlyBears();
        gd.addToExile(player1.getId(), copy);
        gd.exilePlayPermissions.put(copy.getId(), player1.getId());
        gd.exilePlayPermissionsExpireAtTurnEnd.put(copy.getId(), gd.turnNumber + 2);
        gd.exilePlayAnyManaType.add(copy.getId());
        gd.discordCopySourcePermanents.put(copy.getId(), discord.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return copy;
    }
}
