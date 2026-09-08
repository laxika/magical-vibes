package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatchworkCrawler.class, ProdigalSorcerer.class, GrizzlyBears.class})
class PatchworkCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card from your graveyard and puts a +1/+1 counter on Patchwork Crawler")
    void exilesCreatureCardAndGrows() {
        Permanent crawler = addCrawlerReady(player1);
        Card creatureCard = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creatureCard)));
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
        harness.setGraveyard(player2, new ArrayList<>(List.of(creatureCard)));
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
        harness.setGraveyard(player1, new ArrayList<>(List.of(creatureCard)));
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, 0, null, creatureCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, crawler).grantedActivatedAbilities();
        assertThat(granted).hasSize(1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(crawler.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not gain abilities before a creature card is exiled")
    void hasNoGainedAbilitiesBeforeExile() {
        Permanent crawler = addCrawlerReady(player1);

        assertThat(gqs.computeStaticBonus(gd, crawler).grantedActivatedAbilities()).isEmpty();
    }

    private Permanent addCrawlerReady(Player player) {
        return addCreatureReady(player, new PatchworkCrawler());
    }

    private void addManaForAbility(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
