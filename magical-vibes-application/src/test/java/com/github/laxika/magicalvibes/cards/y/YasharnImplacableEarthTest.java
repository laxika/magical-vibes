package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Greed;
import com.github.laxika.magicalvibes.cards.h.Harrow;
import com.github.laxika.magicalvibes.cards.k.KazuulsCliffs;
import com.github.laxika.magicalvibes.cards.k.KazuulsFury;
import com.github.laxika.magicalvibes.cards.l.LithoformBlight;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YasharnImplacableEarth.class, Forest.class, Plains.class, Atog.class, Spellbook.class,
        Greed.class, Harrow.class, KazuulsFury.class, KazuulsCliffs.class, LithoformBlight.class,
        ObNixilisUnshackled.class, AshayaSoulOfTheWild.class})
class YasharnImplacableEarthTest extends BaseCardTest {

    @Test
    void entersAndSearchesForAForestAndAPlains() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        Card other = new Spellbook();
        harness.setLibrary(player1, List.of(forest, plains, other));
        harness.setHand(player1, List.of(new YasharnImplacableEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
    }

    @Test
    void preventsLifePaymentsAndNonlandPermanentSacrificesAsCosts() {
        harness.addToBattlefield(player1, new YasharnImplacableEarth());
        harness.addToBattlefield(player1, new Greed());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pay life");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.addToBattlefield(player1, new Atog());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifact.getId()));
    }

    @Test
    void stillAllowsSacrificingALandAsACost() {
        harness.addToBattlefield(player1, new YasharnImplacableEarth());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Harrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithSacrifice(player1, 0, null, land.getId());

        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void canFindOnlyPlainsWhenNoForestExists() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        castYasharnAndResolveTrigger();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canFindOnlyForestWhenNoPlainsExists() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castYasharnAndResolveTrigger();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDeclineForestAndStillFindPlains() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));
        castYasharnAndResolveTrigger();

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canFindForestAndDeclinePlains() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));
        castYasharnAndResolveTrigger();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDeclineBothAvailableLands() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));
        castYasharnAndResolveTrigger();

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, plains);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void searchCompletesWithNoMatchingLands() {
        YasharnImplacableEarth other = new YasharnImplacableEarth();
        harness.setLibrary(player1, List.of(other));
        castYasharnAndResolveTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void findingBothLandsTriggersAnOpponentsSearchAbilityOnlyOnce() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        castYasharnAndResolveTrigger();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void allowsSacrificeAndLifeLossFromAResolvingAbility() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        harness.setLife(player1, 20);
        castYasharnAndResolveTrigger();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yasharn, Implacable Earth");
        harness.assertNotOnBattlefield(player1, "Yasharn, Implacable Earth");
        harness.assertLife(player1, 10);
    }

    @Test
    void opposingYasharnPreventsLifePaymentsForActivatedAbilities() {
        harness.addToBattlefield(player2, new YasharnImplacableEarth());
        harness.addToBattlefield(player1, new Greed());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pay life");

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeYasharnToCastASpell() {
        Permanent yasharn = harness.addToBattlefieldAndReturn(player1, new YasharnImplacableEarth());
        harness.setHand(player1, List.of(new KazuulsFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, player2.getId(), yasharn.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Yasharn, Implacable Earth");
        harness.assertInHand(player1, "Kazuul's Fury");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blocksLifePaymentForManaAbilitiesButAllowsManaWithoutLifePayment() {
        harness.addToBattlefield(player2, new YasharnImplacableEarth());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LithoformBlight());
        aura.setAttachedTo(forest.getId());
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pay life");

        harness.assertLife(player1, 20);
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isOne();
        harness.assertLife(player1, 20);
    }

    @Test
    void canSacrificeALandCreatureToCastASpell() {
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        Permanent yasharn = harness.addToBattlefieldAndReturn(player1, new YasharnImplacableEarth());
        harness.setHand(player1, List.of(new KazuulsFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), yasharn.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yasharn, Implacable Earth");
        harness.assertLife(player2, 16);
    }

    private void castYasharnAndResolveTrigger() {
        harness.setHand(player1, List.of(new YasharnImplacableEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
