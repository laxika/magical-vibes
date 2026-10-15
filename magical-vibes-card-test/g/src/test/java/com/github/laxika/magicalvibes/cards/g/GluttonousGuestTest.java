package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.s.SculptingSteel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GluttonousGuest.class, GrizzlyBears.class, EvolvingWilds.class, SculptingSteel.class})
class GluttonousGuestTest extends BaseCardTest {

    @Test
    @DisplayName("When Gluttonous Guest enters, one Blood token is created")
    void etbCreatesOneBloodToken() {
        harness.setHand(player1, List.of(new GluttonousGuest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> bloods = findPermanents(player1, "Blood");
        assertThat(bloods).hasSize(1);
        Permanent blood = bloods.getFirst();
        assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
        assertThat(blood.getCard().isToken()).isTrue();
        assertThat(findPermanents(player2, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a Blood token draws a card and Guest gains 1 life")
    void bloodSacrificeDrawsAndGainsLife() {
        harness.setHand(player1, List.of(new GluttonousGuest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent blood = findPermanent(player1, "Blood");
        int bloodIdx = gd.playerBattlefields.get(player1.getId()).indexOf(blood);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, bloodIdx, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Blood");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Sacrificing a non-Blood permanent does not gain life")
    void nonBloodSacrificeDoesNotGainLife() {
        harness.addToBattlefield(player1, new GluttonousGuest());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        Permanent wilds = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(wilds), null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Evolving Wilds");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Gains 1 life when a Blood token is sacrificed")
    void gainsLifeWhenBloodSacrificed() {
        harness.enterBattlefieldAndReturn(player1, new GluttonousGuest());
        resolveAllTriggers();
        Permanent blood = findPermanent(player1, "Blood");
        int bloodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blood);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, bloodIndex, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("An opponent sacrificing a Blood token does not trigger your Guest")
    void opponentBloodSacrificeDoesNotGainLife() {
        harness.addToBattlefield(player1, new GluttonousGuest());
        harness.enterBattlefieldAndReturn(player2, new GluttonousGuest());
        resolveAllTriggers();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        Permanent blood = findPermanent(player2, "Blood");

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Blood");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Each Guest triggers separately for each Blood token sacrificed")
    void multipleGuestsTriggerForEachSacrifice() {
        harness.enterBattlefieldAndReturn(player1, new GluttonousGuest());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new GluttonousGuest());
        resolveAllTriggers();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (int i = 0; i < 2; i++) {
            Permanent blood = findPermanent(player1, "Blood");
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
            harness.handleCardChosen(player1, 0);
            resolveAllTriggers();
            harness.assertLife(player1, 20 + 2 * (i + 1));
        }
        harness.assertNotOnBattlefield(player1, "Blood");
    }

    @Test
    @DisplayName("Sacrificing a nontoken copy of Blood does not gain life")
    void nontokenBloodSacrificeDoesNotGainLife() {
        harness.enterBattlefieldAndReturn(player1, new GluttonousGuest());
        resolveAllTriggers();
        Permanent blood = findPermanent(player1, "Blood");
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());

        Permanent copy = findPermanents(player1, "Blood").stream()
                .filter(permanent -> !permanent.getId().equals(blood.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().isToken()).isFalse();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(copy), null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).containsExactly(blood);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }
}
