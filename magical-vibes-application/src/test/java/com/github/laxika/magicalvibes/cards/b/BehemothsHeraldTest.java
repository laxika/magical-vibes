package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AkrasanSquire;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.cards.g.Godsire;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
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

@CardUsed({BehemothsHerald.class, BloodthornTaunter.class, DruidOfTheAnima.class,
        AkrasanSquire.class, Godsire.class, WoollyThoctar.class})
class BehemothsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new BehemothsHerald());
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 3);
        return herald;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new BloodthornTaunter());     // red
        harness.addToBattlefield(player1, new DruidOfTheAnima());  // green
        // No white creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Red sacrifice prompt only offers red creatures")
    void redPromptOffersOnlyRedCreatures() {
        setUpHerald();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima());
        harness.addToBattlefieldAndReturn(player1, new AkrasanSquire());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(giantId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one red, one green, and one white creature")
    void payingSacrificesOneOfEachColor() {
        setUpHerald();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima()).getId();
        harness.addToBattlefieldAndReturn(player1, new AkrasanSquire());

        harness.activateAbility(player1, 0, null, null);
        // Red pick, then green pick; the sole remaining white creature is paid automatically.
        harness.handlePermanentChosen(player1, giantId);
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertInGraveyard(player1, "Bloodthorn Taunter");
        harness.assertInGraveyard(player1, "Druid of the Anima");
        harness.assertInGraveyard(player1, "Akrasan Squire");
        // The Herald itself was not sacrificed and the ability is on the stack.
        harness.assertOnBattlefield(player1, "Behemoth's Herald");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Godsire by name and puts it onto the battlefield")
    void resolvingPutsGodsireOntoBattlefield() {
        setUpHerald();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima()).getId();
        harness.addToBattlefieldAndReturn(player1, new AkrasanSquire());

        harness.setLibrary(player1, List.of(new Godsire(), new DruidOfTheAnima()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, giantId);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Godsire"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Godsire");
    }

    @Test
    @DisplayName("The tapped Herald can be sacrificed as the green creature and its ability still resolves")
    void canSacrificeHeraldItself() {
        Permanent herald = setUpHerald();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        harness.addToBattlefield(player1, new AkrasanSquire());
        harness.setLibrary(player1, List.of(new Godsire()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, redId);
        harness.handlePermanentChosen(player1, herald.getId());
        assertThat(herald.isTapped()).isTrue();

        harness.assertInGraveyard(player1, "Behemoth's Herald");
        harness.assertInGraveyard(player1, "Bloodthorn Taunter");
        harness.assertInGraveyard(player1, "Akrasan Squire");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Godsire");
        assertThat(findPermanent(player1, "Godsire").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A multicolored creature cannot pay for multiple sacrifice slots")
    void requiresThreeDistinctCreatures() {
        setUpHerald();
        harness.addToBattlefield(player1, new Godsire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player1, "Godsire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A multicolored creature can pay one slot but must remain available for a later required color")
    void multicoloredCreaturePaysWhiteSlot() {
        Permanent herald = setUpHerald();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        harness.addToBattlefield(player1, new Godsire());

        harness.activateAbility(player1, 0, null, null);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(redId);
        harness.handlePermanentChosen(player1, redId);
        harness.handlePermanentChosen(player1, herald.getId());

        harness.assertInGraveyard(player1, "Bloodthorn Taunter");
        harness.assertInGraveyard(player1, "Behemoth's Herald");
        harness.assertInGraveyard(player1, "Godsire");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("The controller may fail to find even when Godsire is in the library")
    void mayFailToFind() {
        Permanent herald = setUpHerald();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        harness.addToBattlefield(player1, new AkrasanSquire());
        Godsire godsire = new Godsire();
        harness.setLibrary(player1, List.of(godsire));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, redId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(godsire);
        harness.assertNotOnBattlefield(player1, "Godsire");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's white creature cannot be sacrificed to pay the cost")
    void cannotSacrificeOpponentsCreature() {
        setUpHerald();
        harness.addToBattlefield(player1, new BloodthornTaunter());
        harness.addToBattlefield(player2, new AkrasanSquire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player2, "Akrasan Squire");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the Herald's tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent herald = setUpHerald();
        herald.setSummoningSick(true);
        harness.addToBattlefield(player1, new BloodthornTaunter());
        harness.addToBattlefield(player1, new AkrasanSquire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Bloodthorn Taunter");
        harness.assertOnBattlefield(player1, "Akrasan Squire");
    }

    @Test
    @DisplayName("An already tapped Herald cannot activate")
    void cannotActivateWhileTapped() {
        Permanent herald = setUpHerald();
        herald.tap();
        harness.addToBattlefield(player1, new BloodthornTaunter());
        harness.addToBattlefield(player1, new AkrasanSquire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching a library without Godsire finishes without putting another card onto the battlefield")
    void noMatchingCardFinishesSearch() {
        Permanent herald = setUpHerald();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        harness.addToBattlefield(player1, new AkrasanSquire());
        AkrasanSquire unrelatedCard = new AkrasanSquire();
        harness.setLibrary(player1, List.of(unrelatedCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, redId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrelatedCard);
        harness.assertNotOnBattlefield(player1, "Akrasan Squire");
        harness.assertNotOnBattlefield(player1, "Godsire");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two mana is insufficient even when it includes green")
    void requiresThreeMana() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new BehemothsHerald());
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addToBattlefield(player1, new BloodthornTaunter());
        harness.addToBattlefield(player1, new AkrasanSquire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(herald.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Bloodthorn Taunter");
        harness.assertOnBattlefield(player1, "Akrasan Squire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activation's mana payment must include green")
    void requiresGreenMana() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new BehemothsHerald());
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addToBattlefield(player1, new BloodthornTaunter());
        harness.addToBattlefield(player1, new AkrasanSquire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(herald.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Bloodthorn Taunter");
        harness.assertOnBattlefield(player1, "Akrasan Squire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three multicolored creatures can each pay a different sacrifice slot")
    void canSacrificeThreeMulticoloredCreatures() {
        Permanent herald = setUpHerald();
        UUID firstId = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar()).getId();
        UUID secondId = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar()).getId();
        harness.addToBattlefield(player1, new WoollyThoctar());
        harness.setLibrary(player1, List.of(new Godsire()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstId);
        harness.handlePermanentChosen(player1, secondId);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Woolly Thoctar")).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Woolly Thoctar");
        harness.assertOnBattlefield(player1, "Behemoth's Herald");
        assertThat(herald.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Godsire");
    }

    @Test
    @DisplayName("Searching an empty library completes normally after the sacrifices")
    void emptyLibraryFinishesSearch() {
        Permanent herald = setUpHerald();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        harness.addToBattlefield(player1, new AkrasanSquire());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, redId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Behemoth's Herald");
        harness.assertInGraveyard(player1, "Bloodthorn Taunter");
        harness.assertInGraveyard(player1, "Akrasan Squire");
        harness.assertNotOnBattlefield(player1, "Godsire");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
