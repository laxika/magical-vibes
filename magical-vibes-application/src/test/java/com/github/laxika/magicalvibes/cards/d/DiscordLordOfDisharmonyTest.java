package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscordLordOfDisharmony.class, GrizzlyBears.class})
class DiscordLordOfDisharmonyTest extends BaseCardTest {

    @Test
    @DisplayName("creates a random nonland copy with temporary any-mana exile permission")
    void createsRandomNonlandCopy() {
        Permanent discord = harness.addToBattlefieldAndReturn(player1, new DiscordLordOfDisharmony());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
}
