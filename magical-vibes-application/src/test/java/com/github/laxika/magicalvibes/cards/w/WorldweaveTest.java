package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Worldweave.class, Forest.class, GrizzlyBears.class, Shock.class,
        DryadArbor.class, GrafdiggersCage.class})
class WorldweaveTest extends BaseCardTest {

    @Test
    void putsASeekingLandOntoTheBattlefieldTappedWhenCastingACreature() {
        Forest soughtLand = new Forest();
        GrizzlyBears otherLibraryCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new Worldweave());
        harness.setLibrary(player1, List.of(otherLibraryCard, soughtLand));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == soughtLand && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLibraryCard);
    }

    @Test
    void doesNotTriggerForNoncreatureSpells() {
        Forest land = new Forest();
        harness.addToBattlefield(player1, new Worldweave());
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void doesNotTriggerWhenAnOpponentCastsACreature() {
        Forest land = new Forest();
        harness.addToBattlefield(player1, new Worldweave());
        harness.setLibrary(player1, List.of(land));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == land);
    }

    @Test
    void doesNothingWhenTheLibraryContainsNoLands() {
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new Worldweave());
        harness.setLibrary(player1, List.of(libraryCard));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void doesNothingWhenTheLibraryIsEmpty() {
        harness.addToBattlefield(player1, new Worldweave());
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void eachWorldweaveSeeksADifferentLandForTheSameCreatureSpell() {
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.addToBattlefield(player1, new Worldweave());
        harness.addToBattlefield(player1, new Worldweave());
        harness.setLibrary(player1, List.of(firstLand, secondLand));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == firstLand && permanent.isTapped())
                .anyMatch(permanent -> permanent.getCard() == secondLand && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({Worldweave.class, GrizzlyBears.class, DryadArbor.class, GrafdiggersCage.class})
    void soughtDryadArborEntersFromHandDespiteGrafdiggersCage() {
        DryadArbor land = new DryadArbor();
        harness.addToBattlefield(player1, new Worldweave());
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.setLibrary(player1, List.of(land));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotInHand(player1, "Dryad Arbor");
    }
}
