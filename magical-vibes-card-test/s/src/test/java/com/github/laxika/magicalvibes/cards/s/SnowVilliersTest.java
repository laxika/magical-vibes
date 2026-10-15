package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdventurersInn;
import com.github.laxika.magicalvibes.cards.t.TravelingChocobo;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnowVilliers.class, TravelingChocobo.class, AdventurersInn.class})
class SnowVilliersTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures you control and toughness stays 3")
    void powerEqualsControlledCreatures() {
        Permanent snow = addCreatureReady(player1, new SnowVilliers());

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, snow)).isEqualTo(3);

        harness.addToBattlefield(player1, new TravelingChocobo());
        harness.addToBattlefield(player1, new TravelingChocobo());

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, snow)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power counts only creatures controlled by Snow Villiers's controller")
    void powerIgnoresOpponentsCreatures() {
        Permanent snow = addCreatureReady(player1, new SnowVilliers());

        harness.addToBattlefield(player2, new TravelingChocobo());
        harness.addToBattlefield(player2, new TravelingChocobo());

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates when creatures leave the battlefield")
    void powerUpdatesWhenCreaturesChange() {
        Permanent snow = addCreatureReady(player1, new SnowVilliers());
        harness.addToBattlefield(player1, new TravelingChocobo());
        harness.addToBattlefield(player1, new TravelingChocobo());

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Traveling Chocobo"));

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncreature permanents do not increase Snow Villiers's power")
    void powerIgnoresNoncreaturePermanents() {
        Permanent snow = addCreatureReady(player1, new SnowVilliers());
        harness.addToBattlefield(player1, new AdventurersInn());
        harness.addToBattlefield(player2, new AdventurersInn());

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power in the graveyard counts its owner's creatures without counting itself")
    void powerInGraveyardCountsOwnersCreatures() {
        SnowVilliers snow = new SnowVilliers();
        gd.playerGraveyards.get(player1.getId()).add(snow);
        harness.addToBattlefield(player2, new TravelingChocobo());

        assertThat(gqs.getEffectiveCardPower(gd, snow)).isZero();

        harness.addToBattlefield(player1, new TravelingChocobo());

        assertThat(gqs.getEffectiveCardPower(gd, snow)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, snow)).isEqualTo(3);
    }

    @Test
    @DisplayName("Vigilance keeps Snow Villiers untapped when it attacks")
    void attackingDoesNotTapSnow() {
        Permanent snow = addCreatureReady(player1, new SnowVilliers());

        declareAttackers(List.of(0));

        assertThat(snow.isAttacking()).isTrue();
        assertThat(snow.isTapped()).isFalse();
    }
}