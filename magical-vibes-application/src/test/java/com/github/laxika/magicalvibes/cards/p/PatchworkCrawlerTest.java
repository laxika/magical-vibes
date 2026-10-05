package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TibaltCosmicImpostor;
import com.github.laxika.magicalvibes.cards.v.ValkiGodOfLies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatchworkCrawler.class, ProdigalSorcerer.class, GrizzlyBears.class,
        ValkiGodOfLies.class, TibaltCosmicImpostor.class})
class PatchworkCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card from your graveyard and puts a +1/+1 counter on Patchwork Crawler")
    void exilesCreatureCardAndGrows() {
        Permanent crawler = addCrawlerReady(player1);
        Card creatureCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creatureCard));
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(crawler.getId())).containsExactly(creatureCard);
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        addCrawlerReady(player1);
        Card creatureCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creatureCard));
        addManaForAbility(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains and can activate an ability of an exiled creature card")
    void gainsAndActivatesExiledCreatureAbility() {
        Permanent crawler = addCrawlerReady(player1);
        Card creatureCard = new ProdigalSorcerer();
        harness.setGraveyard(player1, List.of(creatureCard));
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(crawler.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not gain abilities before a creature card is exiled")
    void hasNoGainedAbilitiesBeforeExile() {
        addCrawlerReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainedValkiAbilityCannotCopyCardsExiledByCrawlerAbility() {
        Permanent crawler = addCrawlerReady(player1);
        Card valki = new ValkiGodOfLies();
        harness.setGraveyard(player1, List.of(valki));
        addManaForAbility(player1);
        harness.activateAbility(player1, 0, 0, null, valki.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(crawler.getCard()).isInstanceOf(PatchworkCrawler.class);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
    }

    @Test
    void secondActivationForSameTargetDoesNotAddAnotherCounter() {
        Permanent crawler = addCrawlerReady(player1);
        Card creatureCard = new PatchworkCrawler();
        harness.setGraveyard(player1, List.of(creatureCard));
        addManaForAbility(player1);
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(crawler.getId())).containsExactly(creatureCard);
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
    }

    @Test
    void anotherCrawlerDoesNotShareGainedAbilities() {
        addCrawlerReady(player1);
        addCrawlerReady(player1);
        Card creatureCard = new ProdigalSorcerer();
        harness.setGraveyard(player1, List.of(creatureCard));
        addManaForAbility(player1);
        harness.activateAbility(player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    void abilityStillExilesTargetAfterCrawlerLeavesBattlefield() {
        Permanent crawler = addCrawlerReady(player1);
        Card creatureCard = new PatchworkCrawler();
        harness.setGraveyard(player1, List.of(creatureCard));
        addManaForAbility(player1);
        harness.activateAbility(player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, crawler));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creatureCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void returnedCrawlerHasNoConnectionToPreviouslyExiledCards() {
        Permanent crawler = addCrawlerReady(player1);
        Card creatureCard = new ProdigalSorcerer();
        harness.setGraveyard(player1, List.of(creatureCard));
        addManaForAbility(player1);
        harness.activateAbility(player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Card crawlerCard = crawler.getCard();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, crawler));
        harness.castFromHand(player1, crawlerCard, "{1}{U}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureCard);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsAbilitiesFromEveryExiledCreatureCard() {
        Permanent crawler = addCrawlerReady(player1);
        Card first = new ProdigalSorcerer();
        Card second = new ProdigalSorcerer();
        harness.setGraveyard(player1, List.of(first, second));
        addManaForAbility(player1);
        harness.activateAbility(player1, 0, 0, null, first.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        addManaForAbility(player1);
        harness.activateAbility(player1, 0, 0, null, second.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(crawler.isTapped()).isTrue();
        harness.assertLife(player2, 19);
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(4);
    }

    @Test
    void summoningSicknessDoesNotPreventExileButPreventsGainedTapAbility() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new PatchworkCrawler());
        crawler.setSummoningSick(true);
        Card creatureCard = new ProdigalSorcerer();
        harness.setGraveyard(player1, List.of(creatureCard));
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(crawler.getId())).containsExactly(creatureCard);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crawler.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    private Permanent addCrawlerReady(Player player) {
        return addCreatureReady(player, new PatchworkCrawler());
    }

    private void addManaForAbility(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
