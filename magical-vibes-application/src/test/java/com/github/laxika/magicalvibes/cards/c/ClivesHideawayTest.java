package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AdelbertSteiner;
import com.github.laxika.magicalvibes.cards.a.AerithGainsborough;
import com.github.laxika.magicalvibes.cards.b.BarretWallace;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TifaLockhart;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClivesHideaway.class, GrizzlyBears.class, AdelbertSteiner.class,
        AerithGainsborough.class, BarretWallace.class, Plains.class, TifaLockhart.class})
class ClivesHideawayTest extends BaseCardTest {

    private Permanent addHideawayWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new ClivesHideaway());
        GameData gd = harness.getGameData();
        Permanent hideaway = findPermanent(player1, "Clive's Hideaway");
        gd.setImprintedCard(hideaway.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return hideaway;
    }

    @Test
    @DisplayName("Tapping adds colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new ClivesHideaway());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Plays the exiled card when four legendary creatures are controlled")
    void playsExiledCardWithFourLegendaryCreatures() {
        addHideawayWithImprint(new GrizzlyBears());
        harness.addToBattlefield(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new BarretWallace());
        harness.addToBattlefield(player1, new TifaLockhart());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing with fewer than four legendary creatures")
    void doesNothingBelowLegendaryCreatureThreshold() {
        addHideawayWithImprint(new Plains());
        harness.addToBattlefield(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new BarretWallace());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Declining the may choice leaves the card exiled")
    void decliningLeavesCardExiled() {
        addHideawayWithImprint(new GrizzlyBears());
        harness.addToBattlefield(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new BarretWallace());
        harness.addToBattlefield(player1, new TifaLockhart());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
