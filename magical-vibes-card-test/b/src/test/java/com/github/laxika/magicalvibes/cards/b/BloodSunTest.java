package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.FoulOrchard;
import com.github.laxika.magicalvibes.cards.j.JunglebornPioneer;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.d.DunesOfTheDead;
import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodSun.class, GrizzlyBears.class, DunesOfTheDead.class, HostileDesert.class,
        StoneRain.class, EvolvingWilds.class, FoulOrchard.class, Naturalize.class, JunglebornPioneer.class})
class BloodSunTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters")
    void drawsACardWhenItEnters() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BloodSun()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands keep mana abilities but lose non-mana abilities")
    void preservesManaAbilitiesOnly() {
        Permanent hostileDesert = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        hostileDesert.setSummoningSick(false);
        resolveBloodSun();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setGraveyard(player2, List.of(new EvolvingWilds()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hostileDesert);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lands lose their triggered abilities")
    void removesLandTriggeredAbilities() {
        harness.addToBattlefield(player2, new DunesOfTheDead());
        resolveBloodSun();

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Dunes of the Dead"));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Dunes of the Dead");
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }


    @Test
    @DisplayName("The controller's lands lose their enters-tapped ability")
    void ownLandEntersUntapped() {
        resolveBloodSun();
        harness.setHand(player1, List.of(new FoulOrchard()));

        harness.playLand(player1, 0);

        Permanent orchard = findPermanent(player1, "Foul Orchard");
        assertThat(orchard.isTapped()).isFalse();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(orchard), 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The opponent's lands also lose their enters-tapped ability")
    void opposingLandEntersUntapped() {
        resolveBloodSun();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new FoulOrchard()));

        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Foul Orchard").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Removing Blood Sun restores land abilities")
    void removingBloodSunRestoresLandAbilities() {
        harness.addToBattlefield(player1, new HostileDesert());
        resolveBloodSun();
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Blood Sun"));

        harness.setGraveyard(player1, List.of(new EvolvingWilds()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        Permanent desert = findPermanent(player1, "Hostile Desert");
        assertThat(gqs.isCreature(gd, desert)).isTrue();
        assertThat(gqs.getEffectivePower(gd, desert)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, desert)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card instanceof EvolvingWilds);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof EvolvingWilds);

        harness.setHand(player1, List.of(new FoulOrchard()));
        harness.playLand(player1, 0);
        assertThat(findPermanent(player1, "Foul Orchard").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blood Sun does not suppress abilities of nonland creatures")
    void nonlandCreatureStillTriggers() {
        resolveBloodSun();
        harness.setHand(player1, List.of(new JunglebornPioneer()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
    }

    @Test
    @DisplayName("The draw trigger resolves even if Blood Sun leaves first")
    void drawTriggerSurvivesSourceRemoval() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BloodSun(), new Naturalize()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Blood Sun"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Blood Sun");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void resolveBloodSun() {
        harness.setHand(player1, List.of(new BloodSun()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
    }
}
