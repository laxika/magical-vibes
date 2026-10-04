package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EmpyrialArchangel;
import com.github.laxika.magicalvibes.cards.c.CatharticAdept;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
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

@CardUsed({AngelsHerald.class, DruidOfTheAnima.class, AkrasanSquire.class, CatharticAdept.class,
        EmpyrialArchangel.class})
class AngelsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new AngelsHerald());
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 3);
        return herald;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new DruidOfTheAnima());  // green
        harness.addToBattlefield(player1, new AkrasanSquire());   // white
        // No blue creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Green sacrifice prompt only offers green creatures")
    void greenPromptOffersOnlyGreenCreatures() {
        setUpHerald();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima()).getId();
        harness.addToBattlefieldAndReturn(player1, new AkrasanSquire());
        harness.addToBattlefieldAndReturn(player1, new CatharticAdept());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(greenId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one green, one white, and one blue creature")
    void payingSacrificesOneOfEachColor() {
        setUpHerald();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima()).getId();
        UUID whiteId = harness.addToBattlefieldAndReturn(player1, new AkrasanSquire()).getId();
        harness.addToBattlefieldAndReturn(player1, new CatharticAdept());

        harness.activateAbility(player1, 0, null, null);
        // Green pick, then white pick; the sole remaining blue creature is paid automatically.
        harness.handlePermanentChosen(player1, greenId);
        harness.handlePermanentChosen(player1, whiteId);

        harness.assertInGraveyard(player1, "Druid of the Anima");
        harness.assertInGraveyard(player1, "Akrasan Squire");
        harness.assertInGraveyard(player1, "Cathartic Adept");
        // The Herald itself was not sacrificed and the ability is on the stack.
        harness.assertOnBattlefield(player1, "Angel's Herald");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Empyrial Archangel by name and puts it onto the battlefield")
    void resolvingPutsEmpyrialArchangelOntoBattlefield() {
        setUpHerald();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima()).getId();
        UUID whiteId = harness.addToBattlefieldAndReturn(player1, new AkrasanSquire()).getId();
        harness.addToBattlefieldAndReturn(player1, new CatharticAdept());

        harness.setLibrary(player1, List.of(new EmpyrialArchangel(), new DruidOfTheAnima()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, greenId);
        harness.handlePermanentChosen(player1, whiteId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Empyrial Archangel"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Empyrial Archangel");
    }

    @Test
    @DisplayName("The tapped Herald can be sacrificed as the white creature and its ability still resolves")
    void canSacrificeHeraldItself() {
        Permanent herald = setUpHerald();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima()).getId();
        harness.addToBattlefield(player1, new CatharticAdept());
        harness.setLibrary(player1, List.of(new EmpyrialArchangel()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, greenId);
        harness.handlePermanentChosen(player1, herald.getId());

        assertThat(herald.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Angel's Herald");
        harness.assertInGraveyard(player1, "Druid of the Anima");
        harness.assertInGraveyard(player1, "Cathartic Adept");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Empyrial Archangel");
        assertThat(findPermanent(player1, "Empyrial Archangel").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A multicolored creature cannot pay for multiple sacrifice slots")
    void requiresThreeDistinctCreatures() {
        setUpHerald();
        harness.addToBattlefield(player1, new EmpyrialArchangel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player1, "Empyrial Archangel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A multicolored creature can pay one slot but must remain available for a later required color")
    void multicoloredCreaturePaysBlueSlot() {
        Permanent herald = setUpHerald();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima()).getId();
        harness.addToBattlefield(player1, new EmpyrialArchangel());

        harness.activateAbility(player1, 0, null, null);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(greenId);
        harness.handlePermanentChosen(player1, greenId);
        harness.handlePermanentChosen(player1, herald.getId());

        harness.assertInGraveyard(player1, "Druid of the Anima");
        harness.assertInGraveyard(player1, "Angel's Herald");
        harness.assertInGraveyard(player1, "Empyrial Archangel");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("The controller may fail to find even when Empyrial Archangel is in the library")
    void mayFailToFind() {
        Permanent herald = setUpHerald();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima()).getId();
        harness.addToBattlefield(player1, new CatharticAdept());
        EmpyrialArchangel angel = new EmpyrialArchangel();
        harness.setLibrary(player1, List.of(angel));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, greenId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(angel);
        harness.assertNotOnBattlefield(player1, "Empyrial Archangel");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's blue creature cannot be sacrificed to pay the cost")
    void cannotSacrificeOpponentsCreature() {
        setUpHerald();
        harness.addToBattlefield(player1, new DruidOfTheAnima());
        harness.addToBattlefield(player2, new CatharticAdept());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player2, "Cathartic Adept");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the Herald's tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent herald = setUpHerald();
        herald.setSummoningSick(true);
        harness.addToBattlefield(player1, new DruidOfTheAnima());
        harness.addToBattlefield(player1, new CatharticAdept());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Druid of the Anima");
        harness.assertOnBattlefield(player1, "Cathartic Adept");
    }

    @Test
    @DisplayName("An already tapped Herald cannot activate")
    void cannotActivateWhileTapped() {
        Permanent herald = setUpHerald();
        herald.tap();
        harness.addToBattlefield(player1, new DruidOfTheAnima());
        harness.addToBattlefield(player1, new CatharticAdept());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching a library without Empyrial Archangel finishes without putting another card onto the battlefield")
    void noMatchingCardFinishesSearch() {
        Permanent herald = setUpHerald();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima()).getId();
        harness.addToBattlefield(player1, new CatharticAdept());
        AkrasanSquire unrelatedCard = new AkrasanSquire();
        harness.setLibrary(player1, List.of(unrelatedCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, greenId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrelatedCard);
        harness.assertNotOnBattlefield(player1, "Akrasan Squire");
        harness.assertNotOnBattlefield(player1, "Empyrial Archangel");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two mana is insufficient even when it includes white")
    void requiresThreeMana() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new AngelsHerald());
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addToBattlefield(player1, new DruidOfTheAnima());
        harness.addToBattlefield(player1, new CatharticAdept());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(herald.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Druid of the Anima");
        harness.assertOnBattlefield(player1, "Cathartic Adept");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activation's mana payment must include white")
    void requiresWhiteMana() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new AngelsHerald());
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addToBattlefield(player1, new DruidOfTheAnima());
        harness.addToBattlefield(player1, new CatharticAdept());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(herald.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Druid of the Anima");
        harness.assertOnBattlefield(player1, "Cathartic Adept");
        assertThat(gd.stack).isEmpty();
    }
}
