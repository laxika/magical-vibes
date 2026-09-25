package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AetherShockwave;
import com.github.laxika.magicalvibes.cards.c.CurtainOfLight;
import com.github.laxika.magicalvibes.cards.i.IdeasUnbound;
import com.github.laxika.magicalvibes.cards.o.OppressiveWill;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KahoMinamoHistorian.class, SpiritualVisit.class, OppressiveWill.class,
        CurtainOfLight.class, AetherShockwave.class, IdeasUnbound.class})
class KahoMinamoHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability exiles up to three instant cards")
    void searchesForUpToThreeInstants() {
        SpiritualVisit firstVisit = new SpiritualVisit();
        OppressiveWill oppressiveWill = new OppressiveWill();
        CurtainOfLight curtainOfLight = new CurtainOfLight();
        AetherShockwave aetherShockwave = new AetherShockwave();
        IdeasUnbound ideasUnbound = new IdeasUnbound();
        harness.setLibrary(player1, List.of(firstVisit, oppressiveWill, curtainOfLight,
                aetherShockwave, ideasUnbound));
        harness.setHand(player1, List.of(new KahoMinamoHistorian()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(4);
        assertThat(search.params().remainingCount()).isEqualTo(3);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        Permanent kaho = findPermanent(player1, "Kaho, Minamo Historian");
        assertThat(gd.getCardsExiledByPermanent(kaho.getId()))
                .containsExactlyInAnyOrder(firstVisit, oppressiveWill, curtainOfLight);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(aetherShockwave, ideasUnbound);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability may stop after fewer than three instants")
    void mayStopSearchingBeforeThreeInstants() {
        SpiritualVisit visit = new SpiritualVisit();
        OppressiveWill oppressiveWill = new OppressiveWill();
        IdeasUnbound ideasUnbound = new IdeasUnbound();
        harness.setLibrary(player1, List.of(visit, oppressiveWill, ideasUnbound));
        harness.setHand(player1, List.of(new KahoMinamoHistorian()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        Permanent kaho = findPermanent(player1, "Kaho, Minamo Historian");
        assertThat(gd.getCardsExiledByPermanent(kaho.getId())).containsExactly(visit);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(oppressiveWill, ideasUnbound);
    }

    @Test
    @DisplayName("The activated ability free-casts exactly one exiled instant with the chosen mana value")
    void castsOneExiledInstantWithExactManaValue() {
        Permanent kaho = harness.addToBattlefieldAndReturn(player1, new KahoMinamoHistorian());
        kaho.setSummoningSick(false);
        SpiritualVisit firstVisit = new SpiritualVisit();
        SpiritualVisit secondVisit = new SpiritualVisit();
        AetherShockwave wrongManaValue = new AetherShockwave();
        gd.addToExile(player1.getId(), firstVisit, kaho.getId());
        gd.addToExile(player1.getId(), secondVisit, kaho.getId());
        gd.addToExile(player1.getId(), wrongManaValue, kaho.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spirit");
        assertThat(gd.getCardsExiledByPermanent(kaho.getId()))
                .contains(secondVisit)
                .contains(wrongManaValue)
                .hasSize(2);
    }

    @Test
    @DisplayName("The activated ability does not offer an exiled spell with a different mana value")
    void doesNotOfferWrongManaValue() {
        Permanent kaho = harness.addToBattlefieldAndReturn(player1, new KahoMinamoHistorian());
        kaho.setSummoningSick(false);
        AetherShockwave wrongManaValue = new AetherShockwave();
        gd.addToExile(player1.getId(), wrongManaValue, kaho.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(kaho.getId())).containsExactly(wrongManaValue);
    }

    @Test
    @DisplayName("The activated ability may decline the free cast")
    void mayDeclineFreeCast() {
        Permanent kaho = harness.addToBattlefieldAndReturn(player1, new KahoMinamoHistorian());
        kaho.setSummoningSick(false);
        SpiritualVisit visit = new SpiritualVisit();
        gd.addToExile(player1.getId(), visit, kaho.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Spirit"));
        assertThat(gd.getCardsExiledByPermanent(kaho.getId())).containsExactly(visit);
    }
}
