package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PrinceOfThralls;
import com.github.laxika.magicalvibes.cards.k.KathariScreecher;
import com.github.laxika.magicalvibes.cards.b.BloodpyreElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonsHerald.class, KathariScreecher.class, DregscapeZombie.class,
        BloodpyreElemental.class, PrinceOfThralls.class})
class DemonsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new DemonsHerald());
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 3);
        return herald;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new KathariScreecher()); // blue
        harness.addToBattlefield(player1, new DregscapeZombie());  // black
        // No red creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Blue sacrifice prompt only offers blue creatures")
    void bluePromptOffersOnlyBlueCreatures() {
        setUpHerald();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new KathariScreecher()).getId();
        harness.addToBattlefieldAndReturn(player1, new DregscapeZombie());
        harness.addToBattlefieldAndReturn(player1, new BloodpyreElemental());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(wizardId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one blue, one black, and one red creature")
    void payingSacrificesOneOfEachColor() {
        Permanent herald = setUpHerald();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new KathariScreecher()).getId();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie()).getId();
        harness.addToBattlefieldAndReturn(player1, new BloodpyreElemental());

        harness.activateAbility(player1, 0, null, null);
        // Blue pick, then black pick; the sole remaining red creature is paid automatically.
        harness.handlePermanentChosen(player1, wizardId);
        harness.handlePermanentChosen(player1, zombiesId);

        harness.assertInGraveyard(player1, "Kathari Screecher");
        harness.assertInGraveyard(player1, "Dregscape Zombie");
        harness.assertInGraveyard(player1, "Bloodpyre Elemental");
        // The Herald itself was not sacrificed and the ability is on the stack.
        harness.assertOnBattlefield(player1, "Demon's Herald");
        assertThat(herald.isTapped()).isTrue();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Prince of Thralls by name and puts it onto the battlefield")
    void resolvingPutsPrinceOfThrallsOntoBattlefield() {
        setUpHerald();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new KathariScreecher()).getId();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie()).getId();
        harness.addToBattlefieldAndReturn(player1, new BloodpyreElemental());

        harness.setLibrary(player1, List.of(new PrinceOfThralls(), new DregscapeZombie()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, wizardId);
        harness.handlePermanentChosen(player1, zombiesId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Prince of Thralls"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Prince of Thralls");
        assertThat(findPermanent(player1, "Prince of Thralls").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Dregscape Zombie");
    }

    @Test
    void canSacrificeHeraldAndStillResolveItsAbility() {
        Permanent herald = setUpHerald();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new KathariScreecher()).getId();
        harness.addToBattlefield(player1, new BloodpyreElemental());
        harness.setLibrary(player1, List.of(new PrinceOfThralls()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, blueId);
        harness.handlePermanentChosen(player1, herald.getId());

        harness.assertInGraveyard(player1, "Demon's Herald");
        harness.assertInGraveyard(player1, "Kathari Screecher");
        harness.assertInGraveyard(player1, "Bloodpyre Elemental");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Prince of Thralls");
    }

    @Test
    void multicoloredCreatureCannotPayMoreThanOneSacrifice() {
        setUpHerald();
        harness.addToBattlefield(player1, new PrinceOfThralls());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player1, "Demon's Herald");
        harness.assertOnBattlefield(player1, "Prince of Thralls");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canUseMulticoloredCreatureForRedSacrifice() {
        Permanent herald = setUpHerald();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new KathariScreecher()).getId();
        harness.addToBattlefield(player1, new PrinceOfThralls());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, blueId);
        harness.handlePermanentChosen(player1, herald.getId());

        harness.assertInGraveyard(player1, "Demon's Herald");
        harness.assertInGraveyard(player1, "Kathari Screecher");
        harness.assertInGraveyard(player1, "Prince of Thralls");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    void cannotSacrificeOpponentsCreatureToPayCost() {
        setUpHerald();
        harness.addToBattlefield(player1, new KathariScreecher());
        harness.addToBattlefield(player2, new BloodpyreElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player2, "Bloodpyre Elemental");
    }

    @Test
    void searchWithNoPrinceStillPaysSacrificesAndCompletes() {
        Permanent herald = setUpHerald();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new KathariScreecher()).getId();
        harness.addToBattlefield(player1, new BloodpyreElemental());
        harness.setLibrary(player1, List.of(new DregscapeZombie()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, blueId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Demon's Herald");
        harness.assertInGraveyard(player1, "Kathari Screecher");
        harness.assertInGraveyard(player1, "Bloodpyre Elemental");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Dregscape Zombie");
    }

    @Test
    void canFailToFindEvenWhenPrinceIsInLibrary() {
        Permanent herald = setUpHerald();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new KathariScreecher()).getId();
        harness.addToBattlefield(player1, new BloodpyreElemental());
        harness.setLibrary(player1, List.of(new PrinceOfThralls()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, blueId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Prince of Thralls");
    }

    @Test
    void summoningSickHeraldCannotActivate() {
        Permanent herald = setUpHerald();
        herald.setSummoningSick(true);
        harness.addToBattlefield(player1, new KathariScreecher());
        harness.addToBattlefield(player1, new BloodpyreElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedHeraldCannotActivate() {
        Permanent herald = setUpHerald();
        herald.tap();
        harness.addToBattlefield(player1, new KathariScreecher());
        harness.addToBattlefield(player1, new BloodpyreElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaCannotActivate() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new DemonsHerald());
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player1, new KathariScreecher());
        harness.addToBattlefield(player1, new BloodpyreElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }
}
