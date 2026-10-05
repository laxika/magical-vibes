package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.l.LeadenMyr;
import com.github.laxika.magicalvibes.cards.s.SilverMyr;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrTurbine.class, GoldMyr.class, IronMyr.class, LeadenMyr.class, SilverMyr.class,
        CopperMyr.class, LlanowarElves.class, MyrSire.class})
class MyrTurbineTest extends BaseCardTest {

    // ===== First ability: {T}: Create a 1/1 colorless Myr artifact creature token =====

    @Test
    @DisplayName("First ability creates a 1/1 colorless Myr artifact creature token")
    void firstAbilityCreatesMyrToken() {
        harness.addToBattlefield(player1, new MyrTurbine());

        Permanent turbine = findPermanent(player1, "Myr Turbine");
        turbine.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Myr");
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.MYR);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("First ability taps the Turbine")
    void firstAbilityTapsTurbine() {
        harness.addToBattlefield(player1, new MyrTurbine());

        Permanent turbine = findPermanent(player1, "Myr Turbine");
        turbine.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(turbine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate first ability when already tapped")
    void cannotActivateFirstAbilityWhenTapped() {
        harness.addToBattlefield(player1, new MyrTurbine());

        Permanent turbine = findPermanent(player1, "Myr Turbine");
        turbine.setSummoningSick(false);
        turbine.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Second ability: {T}, Tap five untapped Myr you control: Search library =====

    @Test
    @DisplayName("Cannot activate second ability without five untapped Myr")
    void cannotActivateSecondAbilityWithoutFiveMyr() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());

        Permanent turbine = findPermanent(player1, "Myr Turbine");
        turbine.setSummoningSick(false);
        // Only 4 Myr — need 5
        setAllNotSummoningSick(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    @Test
    @DisplayName("Second ability auto-taps when exactly five Myr available")
    void secondAbilityAutoTapsExactlyFiveMyr() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        setAllNotSummoningSick(player1);

        harness.setLibrary(player1, List.of(new GoldMyr()));

        harness.activateAbility(player1, 0, 1, null, null);

        // All 5 Myr should be tapped
        assertThat(findPermanent(player1, "Gold Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Iron Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Silver Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Leaden Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Copper Myr").isTapped()).isTrue();

        // Turbine should also be tapped (from {T} cost)
        assertThat(findPermanent(player1, "Myr Turbine").isTapped()).isTrue();

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Second ability prompts for choice when more than five Myr available")
    void secondAbilityPromptsWhenMoreThanFiveMyr() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.addToBattlefield(player1, new MyrSire());
        setAllNotSummoningSick(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        // Should prompt for choice since 6 Myr > 5 required
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Completing five tap choices puts ability on stack")
    void completingFiveTapChoicesPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.addToBattlefield(player1, new MyrSire());
        setAllNotSummoningSick(player1);

        harness.setLibrary(player1, List.of(new GoldMyr()));

        UUID goldMyrId = findPermanent(player1, "Gold Myr").getId();
        UUID ironMyrId = findPermanent(player1, "Iron Myr").getId();
        UUID silverMyrId = findPermanent(player1, "Silver Myr").getId();
        UUID leadenMyrId = findPermanent(player1, "Leaden Myr").getId();
        UUID copperMyrId = findPermanent(player1, "Copper Myr").getId();

        harness.activateAbility(player1, 0, 1, null, null);

        // Choose 5 Myr one at a time
        harness.handlePermanentChosen(player1, goldMyrId);
        harness.handlePermanentChosen(player1, ironMyrId);
        harness.handlePermanentChosen(player1, silverMyrId);
        harness.handlePermanentChosen(player1, leadenMyrId);
        harness.handlePermanentChosen(player1, copperMyrId);

        // Ability should now be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        // Chosen Myr should be tapped
        assertThat(findPermanent(player1, "Gold Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Iron Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Silver Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Leaden Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Copper Myr").isTapped()).isTrue();

        // Unchosen Myr Sire should remain untapped
        assertThat(findPermanent(player1, "Myr Sire").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving second ability searches library for Myr creature")
    void resolvingSecondAbilitySearchesForMyrCreature() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        setAllNotSummoningSick(player1);

        // Seed library with a Myr creature and a non-Myr
        harness.setLibrary(player1, List.of(new MyrSire(), new LlanowarElves()));

        // Exactly 5 Myr -> auto-tap
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Should prompt for library search
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Only Myr creature cards should be available
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.MYR));

        // Choose Myr Sire
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Myr Sire should be on the battlefield
        harness.assertOnBattlefield(player1, "Myr Sire");
    }

    @Test
    @DisplayName("Non-Myr creatures are excluded from library search")
    void nonMyrCreaturesExcludedFromSearch() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        setAllNotSummoningSick(player1);

        // Library has only non-Myr creatures
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // No matching cards — library should be shuffled and search should end
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Tapped Myr do not count toward the five required")
    void tappedMyrDoNotCount() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        setAllNotSummoningSick(player1);

        // Tap one Myr — only 4 untapped remain
        findPermanent(player1, "Gold Myr").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    @Test
    @DisplayName("A newly entered noncreature Turbine can create a token")
    void newlyEnteredTurbineCanCreateToken() {
        harness.addToBattlefield(player1, new MyrTurbine());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Myr")).hasSize(1);
        assertThat(findPermanent(player1, "Myr Turbine").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Myr can pay the second ability's tap cost")
    void summoningSickMyrCanPayTapCost() {
        harness.addToBattlefield(player1, new MyrTurbine());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new MyrSire());
        }
        harness.setLibrary(player1, List.of(new MyrSire()));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(findPermanents(player1, "Myr Sire")).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Myr Sire")).hasSize(6);
        assertThat(findPermanents(player1, "Myr Sire").getLast().isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opposing Myr cannot pay the second ability's tap cost")
    void opposingMyrCannotPayTapCost() {
        harness.addToBattlefield(player1, new MyrTurbine());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new MyrSire());
        }
        harness.addToBattlefield(player2, new MyrSire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
        assertThat(findPermanent(player2, "Myr Sire").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A restricted Myr search may find nothing even with a matching card")
    void mayDeclineToFindMatchingMyr() {
        harness.addToBattlefield(player1, new MyrTurbine());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new MyrSire());
        }
        MyrSire libraryMyr = new MyrSire();
        harness.setLibrary(player1, List.of(libraryMyr));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(findPermanents(player1, "Myr Sire")).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryMyr);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }


    private void setAllNotSummoningSick(Player player) {
        gd.playerBattlefields.get(player.getId()).forEach(p -> p.setSummoningSick(false));
    }
}
