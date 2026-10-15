package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.c.CleansingWildfire;
import com.github.laxika.magicalvibes.cards.c.CrackleWithPower;
import com.github.laxika.magicalvibes.cards.d.DualcasterMage;
import com.github.laxika.magicalvibes.cards.e.Electrodominance;
import com.github.laxika.magicalvibes.cards.e.ExplosiveSingularity;
import com.github.laxika.magicalvibes.cards.g.Guttersnipe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SeasonedPyromancer;
import com.github.laxika.magicalvibes.cards.s.SiegeGangCommander;
import com.github.laxika.magicalvibes.cards.t.TerrorOfThePeaks;
import com.github.laxika.magicalvibes.cards.u.UnexpectedWindfall;
import com.github.laxika.magicalvibes.cards.v.VolcanicFallout;
import com.github.laxika.magicalvibes.cards.y.YoungPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaylasKindling.class, Abrade.class, Banefire.class, CleansingWildfire.class,
        CrackleWithPower.class, DualcasterMage.class, Electrodominance.class,
        ExplosiveSingularity.class, Guttersnipe.class, LightningBolt.class,
        SeasonedPyromancer.class, SiegeGangCommander.class, TerrorOfThePeaks.class,
        UnexpectedWindfall.class, VolcanicFallout.class, YoungPyromancer.class})
class KaylasKindlingTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Kayla's Kindling deals 2 damage to any target")
    void enterDealsDamageToTarget() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KaylasKindling()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("At upkeep, Kayla's Kindling drafts and exiles a spellbook card with cast permission")
    void upkeepDraftsAndExilesSpellbookCard() {
        harness.addToBattlefield(player1, new KaylasKindling());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(drafted);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drafted);
        assertThat(gd.exilePlayPermissions).containsEntry(drafted.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(drafted.getId());
    }

    @Test
    @DisplayName("The drafted card's cast permission expires at end of turn")
    void draftedCardPermissionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new KaylasKindling());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(drafted.getId());
        assertThat(gd.findExiledCard(drafted.getId())).isNotNull();
    }
}
