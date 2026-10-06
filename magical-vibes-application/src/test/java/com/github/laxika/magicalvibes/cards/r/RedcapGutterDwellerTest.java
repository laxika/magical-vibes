package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedcapGutterDweller.class, Forest.class, GrizzlyBears.class})
class RedcapGutterDwellerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Rat tokens that can't block")
    void entersWithNonblockingRatTokens() {
        Permanent redcap = harness.enterBattlefieldAndReturn(player1, new RedcapGutterDweller());
        harness.passBothPriorities();

        List<Permanent> rats = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != redcap)
                .toList();

        assertThat(rats).hasSize(2);
        assertThat(rats).allSatisfy(rat -> assertThat(bls.canBlock(gd, rat)).isFalse());
    }

    @Test
    @DisplayName("Sacrificing another creature adds a counter and exiles the top card for play")
    void sacrificeAddsCounterAndExilesTopCard() {
        Permanent redcap = harness.addToBattlefieldAndReturn(player1, new RedcapGutterDweller());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gqs.getEffectivePower(gd, redcap)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, redcap)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the creature and library unchanged")
    void declineDoesNothing() {
        Permanent redcap = harness.addToBattlefieldAndReturn(player1, new RedcapGutterDweller());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, redcap)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, redcap)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The upkeep ability cannot sacrifice Redcap Gutter-Dweller itself")
    void cannotSacrificeItself() {
        Permanent redcap = harness.addToBattlefieldAndReturn(player1, new RedcapGutterDweller());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(redcap);
        assertThat(gqs.getEffectivePower(gd, redcap)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a Rat still adds a counter when the library is empty")
    void emptyLibraryDoesNotPreventCounter() {
        Permanent redcap = harness.enterBattlefieldAndReturn(player1, new RedcapGutterDweller());
        harness.passBothPriorities();
        Permanent rat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != redcap)
                .findFirst().orElseThrow();
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, rat.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rat).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, redcap)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, redcap)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent redcap = harness.addToBattlefieldAndReturn(player1, new RedcapGutterDweller());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, redcap)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The sacrifice and exile still resolve after Redcap leaves the battlefield")
    void sourceLeavingDoesNotPreventExile() {
        Permanent redcap = harness.enterBattlefieldAndReturn(player1, new RedcapGutterDweller());
        harness.passBothPriorities();
        Permanent rat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != redcap)
                .findFirst().orElseThrow();
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, redcap));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, rat.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(redcap, rat).hasSize(1);
        harness.assertInGraveyard(player1, "Redcap Gutter-Dweller");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }
}
